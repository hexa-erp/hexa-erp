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

/** 売上集計画面。検索条件画面と結果画面をviewパラメーターで切り替える。 */
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
		log.info("売上集計の取得リクエスト");
		Map<String, String> search = ViewModels.search(params);
		// 集計画面には検索結果をすべて表示し、ページ分割は行わない。
		search.remove("page");
		search.remove("pageSize");
		Map<String, List<String>> filterIds = ViewModels.filters(request, search, model, "warehouse", "partner", "item",
				"assignee");
		// フィルター選択画面のIDを、正規化したfilterIdsの値で上書きする。
		criteria.setWarehouseIds(filterIds.get("warehouseIds"));
		criteria.setPartnerIds(filterIds.get("partnerIds"));
		criteria.setItemIds(filterIds.get("itemIds"));
		criteria.setAssigneeIds(filterIds.get("assigneeIds"));
		model.addAttribute("filterSelections", filterSelectionService.getSelections(filterIds));
		model.addAttribute("search", search);
		// 検索を実行しない場合や失敗した場合は、空の結果を画面に渡す。
		SaleStatusVO report = new SaleStatusVO();
		// 検索条件画面（view=search）では検索を実行せず、結果画面でのみ取得する。
		if ("results".equals(params.get("view"))) {
			// 数量・金額欄に数値以外が入力された場合は検索を実行しない。
			if (bindingResult.hasErrors()) {
				model.addAttribute("errorMessage", "検索条件の数量と金額を確認してください。");
			} else {
				try {
					report = statusService.getStatus(criteria);
				} catch (DataAccessException e) {
					log.error("売上集計の取得に失敗しました", e);
					model.addAttribute("errorMessage", "集計を取得できませんでした。検索条件を確認してください。");
				}
			}
		}
		model.addAttribute("monthGroups", report.getMonthGroups());
		model.addAttribute("statusTotals", report.getTotals());
		model.addAttribute("activeMenu", "sale");
		model.addAttribute("pageTitle", "売上集計");
		return "sale/status";
	}
}