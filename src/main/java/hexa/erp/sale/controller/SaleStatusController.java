package hexa.erp.sale.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hexa.erp.common.controller.ViewModels;
import hexa.erp.common.service.FilterSelectionService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/sale/status")
public class SaleStatusController {
	@Setter(onMethod_ = @Autowired)
	private FilterSelectionService filterSelectionService;

	@GetMapping
	public String status(@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("판매 현황 조회 요청");
		Map<String, String> search = ViewModels.search(params);
		// 현황은 검색 결과 전체를 표시하며 페이지 분할하지 않는다.
		search.remove("page");
		search.remove("pageSize");
		Map<String, List<String>> filterIds = ViewModels.filters(request, search, model, "warehouse", "partner", "item",
				"assignee");
		model.addAttribute("filterSelections", filterSelectionService.getSelections(filterIds));
		model.addAttribute("search", search);
		model.addAttribute("monthGroups", Collections.emptyList());
		model.addAttribute("statusTotals", Collections.emptyMap());
		model.addAttribute("activeMenu", "sale");
		model.addAttribute("pageTitle", "판매 현황");
		return "sale/status";
	}
}
