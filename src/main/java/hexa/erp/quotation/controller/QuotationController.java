package hexa.erp.quotation.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hexa.erp.common.controller.PostRedirects;
import hexa.erp.common.controller.ViewModels;
import hexa.erp.common.service.FilterSelectionService;
import hexa.erp.quotation.domain.QuotationCriteria;
import hexa.erp.quotation.domain.QuotationLineVO;
import hexa.erp.quotation.domain.QuotationReportCriteria;
import hexa.erp.quotation.domain.QuotationReportVO;
import hexa.erp.quotation.domain.QuotationVO;
import hexa.erp.quotation.service.QuotationReportService;
import hexa.erp.quotation.service.QuotationService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/quotation")
public class QuotationController {
	@Setter(onMethod_ = @Autowired)
	private FilterSelectionService filterSelectionService;

	@Setter(onMethod_ = @Autowired)
	private QuotationService service;

	@Setter(onMethod_ = @Autowired)
	private QuotationReportService reportService;

	// 견적서 조회 및 페이징
	@GetMapping("/list")
	public String list(QuotationCriteria criteria, @RequestParam Map<String, String> params, Model model) {
		log.info("견적서 목록 조회 요청");

		// 페이징 처리
		int totalCount = service.getTotal(criteria);
		int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / criteria.getPageSize()));
		criteria.setPage(Math.min(criteria.getPage(), totalPages));

		// 검색 조건, 페이징 상태 유지
		Map<String, String> search = ViewModels.search(params);
		search.put("keyword", criteria.getKeyword());
		search.put("progressStatus", criteria.getProgressStatus());
		search.put("page", String.valueOf(criteria.getPage()));
		search.put("pageSize", String.valueOf(criteria.getPageSize()));

		model.addAttribute("quotationList", service.getList(criteria));
		model.addAttribute("search", search);
		model.addAttribute("page", criteria.getPage());
		model.addAttribute("pageSize", criteria.getPageSize());
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("basePath", "/quotation/list");
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "견적서 조회");

		return "quotation/list";
	}

	// 견적서 신규 입력 및 기존 견적서 조회
	@GetMapping("/form")
	public String form(@RequestParam(value = "id", required = false) Long id,
			@RequestParam(value = "quotationId", required = false) Long quotationId, HttpServletRequest request,
			RedirectAttributes rttr, Model model) {
		log.info("견적서 입력 화면 요청");

		Long documentId = id == null ? quotationId : id;
		QuotationVO form;

		// 신규 견적서 입력 시 기본값 설정
		if (documentId == null) {
			form = new QuotationVO();

			form.setBusinessDate(LocalDate.now().toString());
			form.setProgressStatus("IN_PROGRESS");
		} else {
			// 기존 견적서 조회
			form = service.get(documentId);
			if (form == null) {
				rttr.addFlashAttribute("errorMessage", "조회할 견적서가 없습니다.");

				return PostRedirects.toList("/quotation/list", request);
			}
		}

		model.addAttribute("form", form);
		model.addAttribute("mode", "create");
		model.addAttribute("isEdit", false);
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "견적서 입력");

		return "quotation/form";
	}

	// 견적서 현황 조회
	@GetMapping("/status")
	public String status(QuotationReportCriteria criteria, BindingResult bindingResult,
			@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("견적서 현황 조회 요청");

		// 검색 조건 및 필터 설정
		prepareReportSearch(criteria, params, request, model);
		QuotationReportVO report = new QuotationReportVO();

		// 견적서 현황 조회
		if ("results".equals(params.get("view"))) {
			if (bindingResult.hasErrors()) {
				model.addAttribute("errorMessage", "검색조건의 수량과 금액을 확인해 주세요.");
			} else {
				try {
					report = reportService.getStatus(criteria);
				} catch (DataAccessException e) {
					log.error("견석서 현황 조회 실패", e);
					model.addAttribute("errorMessage", "현황을 조회하지 못했습니다. 검색조건을 확인해 주세요.");
				}
			}
		}

		model.addAttribute("monthGroups", report.getMonthGroups());
		model.addAttribute("statusTotals", report.getTotals());
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "견적서 현황");
		return "quotation/status";
	}

	// 견적서 전표 조회
	@GetMapping("/statement")
	public String statement(@RequestParam(value = "id", required = false) Long id, HttpServletResponse response,
			Model model) {
		log.info("견적서 전표 조회 요청");

		QuotationVO form = id == null ? null : service.get(id);

		if (form == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			form = new QuotationVO();
		}

		// 견적서 품목별 금액을 더해서 전표 전체 합계 계산
		Map<String, BigDecimal> totals = new LinkedHashMap<>();
		totals.put("supplyAmount", BigDecimal.ZERO);
		totals.put("vatAmount", BigDecimal.ZERO);
		totals.put("totalAmount", BigDecimal.ZERO);

		for (QuotationLineVO line : form.getLines()) {
			totals.put("supplyAmount", totals.get("supplyAmount").add(line.getSupplyAmount()));
			totals.put("vatAmount", totals.get("vatAmount").add(line.getVatAmount()));
			totals.put("totalAmount", totals.get("totalAmount").add(line.getTotalAmount()));
		}

		model.addAttribute("form", form);
		model.addAttribute("statementTotals", totals);
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "견적서 전표");

		return "quotation/statement";
	}

	// 미주문 현황 조회
	@GetMapping("/unordered")
	public String unordered(QuotationReportCriteria criteria, BindingResult bindingResult,
			@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("미주문 현황 조회 요청");

		if (!StringUtils.hasText(criteria.getCutoffDate())) {
			criteria.setCutoffDate(LocalDate.now().toString());
			params.put("cutoffDate", criteria.getCutoffDate());
		}

		// 검색 조건 및 필터 설정
		prepareReportSearch(criteria, params, request, model);

		QuotationReportVO report = new QuotationReportVO();

		// 미주문 현황 조회
		if ("results".equals(params.get("view"))) {
			if (bindingResult.hasErrors()) {
				model.addAttribute("errorMessage", "검색조건의 수량과 금액을 확인해 주세요.");
			} else {
				try {
					report = reportService.getUnordered(criteria);
				} catch (DataAccessException e) {
					log.error("미주문 현황 조회 실패", e);
					model.addAttribute("errorMessage", "미주문 현황을 조회하지 못했습니다. 검색조건을 확인해 주세요.");
				}
			}
		}

		model.addAttribute("monthGroups", report.getMonthGroups());
		model.addAttribute("remainingTotals", report.getTotals());
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "미주문 현황");
		return "quotation/unordered";
	}

	// 검색 조건 및 필터
	private void prepareReportSearch(QuotationReportCriteria criteria, Map<String, String> params,
			HttpServletRequest request, Model model) {
		Map<String, String> search = ViewModels.search(params);
		search.remove("page");
		search.remove("pageSize");

		Map<String, List<String>> filterIds = ViewModels.filters(request, search, model, "warehouse", "partner", "item",
				"assignee");
		criteria.setWarehouseIds(filterIds.get("warehouseIds"));
		criteria.setPartnerIds(filterIds.get("partnerIds"));
		criteria.setItemIds(filterIds.get("itemIds"));
		criteria.setAssigneeIds(filterIds.get("assigneeIds"));

		model.addAttribute("filterSelections", filterSelectionService.getSelections(filterIds));
		model.addAttribute("search", search);
	}

}
