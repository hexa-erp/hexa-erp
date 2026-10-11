package hexa.erp.sale.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hexa.erp.common.controller.ViewModels;
import hexa.erp.common.service.FilterSelectionService;
import hexa.erp.sale.domain.SaleStatusCriteria;
import hexa.erp.sale.domain.SaleStatusVO;
import hexa.erp.sale.service.SaleStatusService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

/** 판매 현황 화면. 검색 화면과 결과 화면을 view 파라미터로 구분한다. */
@Log4j
@Controller
@RequestMapping("/sale/status")
public class SaleStatusController {
	@Setter(onMethod_ = @Autowired)
	private SaleStatusService statusService;
	@Setter(onMethod_ = @Autowired)
	private FilterSelectionService filterSelectionService;

	@GetMapping
	public String status(SaleStatusCriteria criteria, BindingResult bindingResult,
			@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("판매 현황 조회 요청");
		Map<String, String> search = ViewModels.search(params);
		// 현황은 검색 결과 전체를 표시하며 페이지 분할하지 않는다.
		search.remove("page");
		search.remove("pageSize");
		Map<String, List<String>> filterIds = ViewModels.filters(request, search, model, "warehouse", "partner", "item",
				"assignee");
		// 필터 선택창의 ID는 정리된 filterIds 값으로 덮어쓴다.
		criteria.setWarehouseIds(filterIds.get("warehouseIds"));
		criteria.setPartnerIds(filterIds.get("partnerIds"));
		criteria.setItemIds(filterIds.get("itemIds"));
		criteria.setAssigneeIds(filterIds.get("assigneeIds"));
		model.addAttribute("filterSelections", filterSelectionService.getSelections(filterIds));
		model.addAttribute("search", search);
		// 조회하지 않거나 실패하면 빈 결과를 화면에 넘긴다.
		SaleStatusVO report = new SaleStatusVO();
		// 검색 화면(view=search)에서는 조회하지 않고, 결과 화면에서만 조회한다.
		if ("results".equals(params.get("view"))) {
			// 수량·금액 칸에 숫자가 아닌 값이 들어오면 조회하지 않는다.
			if (bindingResult.hasErrors()) {
				model.addAttribute("errorMessage", "검색조건의 수량과 금액을 확인해 주세요.");
			} else {
				try {
					report = statusService.getStatus(criteria);
				} catch (DataAccessException e) {
					log.error("판매 현황 조회 실패", e);
					model.addAttribute("errorMessage", "현황을 조회하지 못했습니다. 검색조건을 확인해 주세요.");
				}
			}
		}
		model.addAttribute("monthGroups", report.getMonthGroups());
		model.addAttribute("statusTotals", report.getTotals());
		model.addAttribute("activeMenu", "sale");
		model.addAttribute("pageTitle", "판매 현황");
		return "sale/status";
	}
}