package hexa.erp.sale.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hexa.erp.common.controller.ViewModels;
import hexa.erp.common.service.FilterSelectionService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/sale/bulk-price")
public class SalePriceController {
	@Setter(onMethod_ = @Autowired)
	private FilterSelectionService filterSelectionService;

	@GetMapping
	public String bulkPrice(@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("판매 단가 일괄 변경 화면 요청");
		Map<String, String> search = ViewModels.search(params);
		// 창고·거래처·품목만 검색조건으로 받는다.
		Map<String, List<String>> filterIds = ViewModels.filters(request, search, model, "warehouse", "partner",
				"item");
		model.addAttribute("filterSelections", filterSelectionService.getSelections(filterIds));
		model.addAttribute("search", search);
		model.addAttribute("salePriceList", Collections.emptyList());
		model.addAttribute("activeMenu", "sale");
		model.addAttribute("pageTitle", "판매 단가 일괄 변경");
		model.addAttribute("pageScript", "sale.js");
		ViewModels.emptyPage(model, 25, "/sale/bulk-price");
		return "sale/bulk-price";
	}

	@PostMapping("/save")
	public String savePrices(HttpServletResponse response, Model model) {
		log.info("판매 단가 일괄 저장 요청");
		return ViewModels.notImplemented(response, model);
	}
}
