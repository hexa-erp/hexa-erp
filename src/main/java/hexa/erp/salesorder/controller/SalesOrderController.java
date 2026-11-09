package hexa.erp.salesorder.controller;

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
import hexa.erp.salesorder.domain.SalesOrderCriteria;
import hexa.erp.salesorder.domain.SalesOrderLineVO;
import hexa.erp.salesorder.domain.SalesOrderReportCriteria;
import hexa.erp.salesorder.domain.SalesOrderReportVO;
import hexa.erp.salesorder.domain.SalesOrderVO;
import hexa.erp.salesorder.service.SalesOrderReportService;
import hexa.erp.salesorder.service.SalesOrderService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/order")
public class SalesOrderController {
	@Setter(onMethod_ = @Autowired)
	private FilterSelectionService filterSelectionService;

	@Setter(onMethod_ = @Autowired)
	private SalesOrderService service;

	@Setter(onMethod_ = @Autowired)
	private SalesOrderReportService reportService;

	// 受注伝票一覧を取得し、ページ分割する。
	@GetMapping("/list")
	public String list(SalesOrderCriteria criteria, @RequestParam Map<String, String> params, Model model) {
		log.info("受注一覧の取得リクエスト");

		// 選択したページを表示し、最終ページを超える番号は最終ページ番号に補正する。
		int totalCount = service.getTotal(criteria);
		int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / criteria.getPageSize()));
		criteria.setPage(Math.min(criteria.getPage(), totalPages));

		// 検索条件とページ情報を維持する。
		Map<String, String> search = ViewModels.search(params);
		search.put("keyword", criteria.getKeyword());
		search.put("progressStatus", criteria.getProgressStatus());
		search.put("page", String.valueOf(criteria.getPage()));
		search.put("pageSize", String.valueOf(criteria.getPageSize()));

		model.addAttribute("orderList", service.getList(criteria));
		model.addAttribute("search", search);
		model.addAttribute("page", criteria.getPage());
		model.addAttribute("pageSize", criteria.getPageSize());
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("basePath", "/order/list");
		model.addAttribute("activeMenu", "order");
		model.addAttribute("pageTitle", "受注一覧");

		return "order/list";
	}

	// 受注伝票の新規入力と既存伝票の取得
	@GetMapping("/form")
	public String form(@RequestParam(value = "id", required = false) Long id, @RequestParam Map<String, String> params,
			@RequestParam(value = "salesOrderId", required = false) Long salesOrderId, HttpServletRequest request,
			Model model, RedirectAttributes rttr) {
		log.info("受注入力画面の表示リクエスト");

		Long documentId = id == null ? salesOrderId : id;
		SalesOrderVO form;

		// IDを指定せずに開いた場合は、本日の日付を設定した空の受注書を表示する。
		if (documentId == null) {
			form = new SalesOrderVO();

			form.setBusinessDate(LocalDate.now().toString());
			form.setProgressStatus("IN_PROGRESS");
		} else {
			// ID指定時は保存済みの基本情報と品目行を表示し、対象がなければ一覧に戻る。
			form = service.get(documentId);
			if (form == null) {
				rttr.addFlashAttribute("errorMessage", "表示対象の受注伝票がありません。");

				return PostRedirects.toList("/order/list", request);
			}
		}

		model.addAttribute("form", form);
		model.addAttribute("mode", "create");
		model.addAttribute("isEdit", false);
		model.addAttribute("activeMenu", "order");
		model.addAttribute("pageTitle", "受注入力");
		return "order/form";
	}

	// 受注集計を取得する。
	@GetMapping("/status")
	public String status(SalesOrderReportCriteria criteria, BindingResult bindingResult,
			@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("受注集計の取得リクエスト");

		// 検索条件とフィルターを設定する。
		prepareReportSearch(criteria, params, request, model);

		SalesOrderReportVO report = new SalesOrderReportVO();

		// 初期表示は検索条件画面とし、「検索」押下後に入力条件で品目行を照会する。
		// 数量・金額を数値に変換できない場合、または照会に失敗した場合は案内を表示する。
		if ("results".equals(params.get("view"))) {
			if (bindingResult.hasErrors()) {
				model.addAttribute("errorMessage", "検索条件の数量と金額を確認してください。");
			} else {
				try {
					report = reportService.getStatus(criteria);
				} catch (DataAccessException e) {
					log.error("受注集計の取得に失敗しました", e);

					model.addAttribute("errorMessage", "集計を取得できませんでした。検索条件を確認してください。");
				}
			}
		}

		model.addAttribute("monthGroups", report.getMonthGroups());
		model.addAttribute("statusTotals", report.getTotals());
		model.addAttribute("activeMenu", "order");
		model.addAttribute("pageTitle", "受注集計");
		return "order/status";
	}

	// 該当受注書の伝票出力画面を開く（照会専用）。
	@GetMapping("/statement")
	public String statement(@RequestParam(value = "id", required = false) Long id, HttpServletResponse response,
			Model model) {
		log.info("受注伝票の表示リクエスト");

		SalesOrderVO form = id == null ? null : service.get(id);
		if (form == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			form = new SalesOrderVO();
		}

		// 受注伝票の品目別金額を合計し、伝票全体の合計を計算する。
		Map<String, BigDecimal> totals = new LinkedHashMap<>();
		totals.put("supplyAmount", BigDecimal.ZERO);
		totals.put("vatAmount", BigDecimal.ZERO);
		totals.put("totalAmount", BigDecimal.ZERO);

		for (SalesOrderLineVO line : form.getLines()) {
			totals.put("supplyAmount", totals.get("supplyAmount").add(line.getSupplyAmount()));
			totals.put("vatAmount", totals.get("vatAmount").add(line.getVatAmount()));
			totals.put("totalAmount", totals.get("totalAmount").add(line.getTotalAmount()));
		}

		model.addAttribute("form", form);
		model.addAttribute("statementTotals", totals);
		model.addAttribute("activeMenu", "order");
		model.addAttribute("pageTitle", "受注伝票");

		return "order/statement";

	}

	// 未売上一覧を取得する。
	@GetMapping("/unsold")
	public String unsold(SalesOrderReportCriteria criteria, BindingResult bindingResult,
			@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("未売上一覧の取得リクエスト");

		// 初期表示では開始日・終了日を指定せず、基準日を当日の日付にする。
		if (!StringUtils.hasText(criteria.getCutoffDate())) {
			criteria.setCutoffDate(LocalDate.now().toString());
			params.put("cutoffDate", criteria.getCutoffDate());
		}

		// 検索条件とフィルターを設定する。
		prepareReportSearch(criteria, params, request, model);
		SalesOrderReportVO report = new SalesOrderReportVO();

		// 初期表示は検索条件画面とし、「検索」押下後に入力条件で品目行を照会する。
		// 数量・金額を数値に変換できない場合、または照会に失敗した場合は案内を表示する。
		if ("results".equals(params.get("view"))) {
			if (bindingResult.hasErrors()) {
				model.addAttribute("errorMessage", "検索条件の数量と金額を確認してください。");
			} else {
				try {
					report = reportService.getUnsold(criteria);
				} catch (DataAccessException e) {
					log.error("未売上一覧の取得に失敗しました", e);

					model.addAttribute("errorMessage", "未売上一覧を取得できませんでした。検索条件を確認してください。");
				}
			}
		}
		model.addAttribute("monthGroups", report.getMonthGroups());
		model.addAttribute("remainingTotals", report.getTotals());
		model.addAttribute("activeMenu", "order");
		model.addAttribute("pageTitle", "未売上一覧");
		return "order/unsold";
	}

	// 検索条件とフィルター
	private void prepareReportSearch(SalesOrderReportCriteria criteria, Map<String, String> params,
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

		// 共通選択画面で複数選択した条件を保持し、選択した値は名称で表示する。
		model.addAttribute("filterSelections", filterSelectionService.getSelections(filterIds));
		model.addAttribute("search", search);
	}
}
