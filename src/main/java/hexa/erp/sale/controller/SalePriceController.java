package hexa.erp.sale.controller;

import java.util.Collections;
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
import hexa.erp.sale.domain.SalePriceCriteria;
import hexa.erp.sale.domain.SalePriceVO;
import hexa.erp.sale.service.SalePriceService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/sale/bulk-price")
public class SalePriceController {
	@Setter(onMethod_ = @Autowired)
	private SalePriceService service;
	@Setter(onMethod_ = @Autowired)
	private FilterSelectionService filterSelectionService;

	@GetMapping
	public String bulkPrice(SalePriceCriteria criteria, BindingResult bindingResult,
			@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("売上単価一括変更画面の表示リクエスト");
		Map<String, String> search = ViewModels.search(params);
		// 倉庫・取引先・品目のみを検索条件として受け取る。
		Map<String, List<String>> filterIds = ViewModels.filters(request, search, model, "warehouse", "partner",
				"item");
		// フィルターIDは、同名の単一IDパラメーターも含めた値で上書きする。
		criteria.setWarehouseIds(filterIds.get("warehouseIds"));
		criteria.setPartnerIds(filterIds.get("partnerIds"));
		criteria.setItemIds(filterIds.get("itemIds"));
		List<SalePriceVO> rows = Collections.emptyList();
		int totalCount = 0;
		int totalPages = 1;
		// 検索条件入力画面（view未指定）では検索を実行せず、結果画面でのみ取得する。
		if ("results".equals(params.get("view"))) {
			if (bindingResult.hasErrors()) {
				model.addAttribute("errorMessage", "検索条件とページ番号を確認してください。");
			} else {
				try {
					totalCount = service.getTotal(criteria);
					totalPages = Math.max(1, (int) Math.ceil((double) totalCount / criteria.getPageSize()));
					// 保存後に行数が減り現在のページがなくなった場合は、最終ページを表示する。
					criteria.setPage(Math.min(criteria.getPage(), totalPages));
					rows = service.getList(criteria);
				} catch (DataAccessException e) {
					log.error("売上単価一括変更の取得に失敗しました", e);
					model.addAttribute("errorMessage", "売上明細行を取得できませんでした。検索条件を確認してください。");
				}
			}
		}
		// 画面にはCriteriaで補正した値（空白除去、ページ範囲）を表示する。
		search.put("keyword", criteria.getKeyword());
		search.put("progressStatus", criteria.getProgressStatus());
		search.put("page", String.valueOf(criteria.getPage()));
		search.put("pageSize", String.valueOf(criteria.getPageSize()));
		model.addAttribute("filterSelections", filterSelectionService.getSelections(filterIds));
		model.addAttribute("search", search);
		model.addAttribute("salePriceList", rows);
		model.addAttribute("page", criteria.getPage());
		model.addAttribute("pageSize", criteria.getPageSize());
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("basePath", "/sale/bulk-price");
		model.addAttribute("activeMenu", "sale");
		model.addAttribute("pageTitle", "売上単価一括変更");
		model.addAttribute("pageScript", "sale.js");
		return "sale/bulk-price";
	}

}
