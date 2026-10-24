package hexa.erp.sale.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hexa.erp.common.controller.PostRedirects;
import hexa.erp.common.controller.ViewModels;
import hexa.erp.sale.domain.SaleCriteria;
import hexa.erp.sale.domain.SaleLineVO;
import hexa.erp.sale.domain.SaleVO;
import hexa.erp.sale.service.SaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/sale")
@RequiredArgsConstructor
public class SaleController {

	private final SaleService service;

	// 売上一覧画面
	@GetMapping("/list")
	public String list(SaleCriteria criteria, @RequestParam Map<String, String> params, Model model) {

		log.info("売上一覧の取得リクエスト");

		int totalCount = service.getTotal(criteria);

		int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / criteria.getPageSize()));

		criteria.setPage(Math.min(criteria.getPage(), totalPages));

		Map<String, String> search = ViewModels.search(params);

		search.put("keyword", criteria.getKeyword());

		search.put("progressStatus", criteria.getProgressStatus());

		search.put("page", String.valueOf(criteria.getPage()));

		search.put("pageSize", String.valueOf(criteria.getPageSize()));

		model.addAttribute("saleList", service.getList(criteria));

		model.addAttribute("search", search);

		model.addAttribute("page", criteria.getPage());

		model.addAttribute("pageSize", criteria.getPageSize());

		model.addAttribute("totalPages", totalPages);

		model.addAttribute("totalCount", totalCount);

		model.addAttribute("basePath", "/sale/list");

		model.addAttribute("activeMenu", "sale");

		model.addAttribute("pageTitle", "売上一覧");

		return "sale/list";
	}

	// 売上入力画面
	@GetMapping("/form")
	public String form(@RequestParam(value = "id", required = false) Long id,
			@RequestParam(value = "saleId", required = false) Long saleId, HttpServletRequest request, Model model,
			RedirectAttributes rttr) {

		log.info("売上入力画面の表示リクエスト");

		Long documentId = id == null ? saleId : id;

		SaleVO form;

		if (documentId == null) {
			form = new SaleVO();

			form.setBusinessDate(LocalDate.now().toString());

			form.setProgressStatus("CONFIRMED");
		} else {
			form = service.get(documentId);

			if (form == null) {
				rttr.addFlashAttribute("errorMessage", "表示対象の売上情報がありません。");

				return PostRedirects.toList("/sale/list", request);
			}
		}

		model.addAttribute("form", form);

		model.addAttribute("mode", documentId == null ? "create" : "edit");

		model.addAttribute("isEdit", documentId != null);

		model.addAttribute("activeMenu", "sale");

		model.addAttribute("pageTitle", "売上入力");

		return "sale/form";
	}

	// 納品書画面
	@GetMapping("/statement")
	public String statement(@RequestParam(value = "id", required = false) Long id, HttpServletResponse response,
			Model model) {

		log.info("納品書の表示リクエスト");

		SaleVO form = id == null ? null : service.get(id);

		if (form == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);

			form = new SaleVO();
		}

		Map<String, BigDecimal> totals = new LinkedHashMap<>();

		totals.put("supplyAmount", BigDecimal.ZERO);

		totals.put("vatAmount", BigDecimal.ZERO);

		totals.put("totalAmount", BigDecimal.ZERO);

		for (SaleLineVO line : form.getLines()) {

			totals.put("supplyAmount", totals.get("supplyAmount").add(line.getSupplyAmount()));

			totals.put("vatAmount", totals.get("vatAmount").add(line.getVatAmount()));

			totals.put("totalAmount", totals.get("totalAmount").add(line.getTotalAmount()));
		}

		model.addAttribute("form", form);

		model.addAttribute("statementTotals", totals);

		model.addAttribute("activeMenu", "sale");

		model.addAttribute("pageTitle", "納品書");

		return "sale/statement";
	}

}
