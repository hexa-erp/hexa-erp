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

	// 見積書を取得し、ページ分割する。
	@GetMapping("/list")
	public String list(QuotationCriteria criteria, @RequestParam Map<String, String> params, Model model) {
		log.info("見積一覧の取得リクエスト");

		// ページ分割を行う。
		int totalCount = service.getTotal(criteria);
		int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / criteria.getPageSize()));
		criteria.setPage(Math.min(criteria.getPage(), totalPages));

		// 検索条件とページ情報を維持する。
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
		model.addAttribute("pageTitle", "見積一覧");

		return "quotation/list";
	}

	// 見積書の新規入力と既存伝票の取得
	@GetMapping("/form")
	public String form(@RequestParam(value = "id", required = false) Long id,
			@RequestParam(value = "quotationId", required = false) Long quotationId, HttpServletRequest request,
			RedirectAttributes rttr, Model model) {
		log.info("見積入力画面の表示リクエスト");

		Long documentId = id == null ? quotationId : id;
		QuotationVO form;

		// 新規見積書の入力時に既定値を設定する。
		if (documentId == null) {
			form = new QuotationVO();

			form.setBusinessDate(LocalDate.now().toString());
			form.setProgressStatus("IN_PROGRESS");
		} else {
			// 既存の見積書を取得する。
			form = service.get(documentId);
			if (form == null) {
				rttr.addFlashAttribute("errorMessage", "表示対象の見積書がありません。");

				return PostRedirects.toList("/quotation/list", request);
			}
		}

		model.addAttribute("form", form);
		model.addAttribute("mode", "create");
		model.addAttribute("isEdit", false);
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "見積入力");

		return "quotation/form";
	}

	// 見積集計を取得する。
	@GetMapping("/status")
	public String status(QuotationReportCriteria criteria, BindingResult bindingResult,
			@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("見積集計の取得リクエスト");

		// 検索条件とフィルターを設定する。
		prepareReportSearch(criteria, params, request, model);
		QuotationReportVO report = new QuotationReportVO();

		// 見積集計を取得する。
		if ("results".equals(params.get("view"))) {
			if (bindingResult.hasErrors()) {
				model.addAttribute("errorMessage", "検索条件の数量と金額を確認してください。");
			} else {
				try {
					report = reportService.getStatus(criteria);
				} catch (DataAccessException e) {
					log.error("見積集計の取得に失敗しました", e);
					model.addAttribute("errorMessage", "集計を取得できませんでした。検索条件を確認してください。");
				}
			}
		}

		model.addAttribute("monthGroups", report.getMonthGroups());
		model.addAttribute("statusTotals", report.getTotals());
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "見積集計");
		return "quotation/status";
	}

	// 見積書を取得する。
	@GetMapping("/statement")
	public String statement(@RequestParam(value = "id", required = false) Long id, HttpServletResponse response,
			Model model) {
		log.info("見積書の表示リクエスト");

		QuotationVO form = id == null ? null : service.get(id);

		if (form == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			form = new QuotationVO();
		}

		// 見積書の品目別金額を合計し、伝票全体の合計を計算する。
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
		model.addAttribute("pageTitle", "見積書");

		return "quotation/statement";
	}

	// 未受注一覧を取得する。
	@GetMapping("/unordered")
	public String unordered(QuotationReportCriteria criteria, BindingResult bindingResult,
			@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("未受注一覧の取得リクエスト");

		if (!StringUtils.hasText(criteria.getCutoffDate())) {
			criteria.setCutoffDate(LocalDate.now().toString());
			params.put("cutoffDate", criteria.getCutoffDate());
		}

		// 検索条件とフィルターを設定する。
		prepareReportSearch(criteria, params, request, model);

		QuotationReportVO report = new QuotationReportVO();

		// 未受注一覧を取得する。
		if ("results".equals(params.get("view"))) {
			if (bindingResult.hasErrors()) {
				model.addAttribute("errorMessage", "検索条件の数量と金額を確認してください。");
			} else {
				try {
					report = reportService.getUnordered(criteria);
				} catch (DataAccessException e) {
					log.error("未受注一覧の取得に失敗しました", e);
					model.addAttribute("errorMessage", "未受注一覧を取得できませんでした。検索条件を確認してください。");
				}
			}
		}

		model.addAttribute("monthGroups", report.getMonthGroups());
		model.addAttribute("remainingTotals", report.getTotals());
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "未受注一覧");
		return "quotation/unordered";
	}

	// 検索条件とフィルター
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
