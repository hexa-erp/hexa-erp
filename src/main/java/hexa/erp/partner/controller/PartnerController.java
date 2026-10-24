package hexa.erp.partner.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hexa.erp.common.controller.ViewModels;
import hexa.erp.partner.domain.PartnerCriteria;
import hexa.erp.partner.service.PartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/master/partner")
public class PartnerController {

	private final PartnerService service;

	@GetMapping
	public String list(PartnerCriteria criteria, @RequestParam Map<String, String> params, Model model) {

		log.info("取引先一覧の取得リクエスト");

		int totalCount = service.getTotal(criteria);
		int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / criteria.getPageSize()));

		criteria.setPage(Math.min(criteria.getPage(), totalPages));

		Map<String, String> search = ViewModels.search(params);
		search.put("keyword", criteria.getKeyword());
		search.put("includeInactive", criteria.getIncludeInactive());
		search.put("page", String.valueOf(criteria.getPage()));
		search.put("pageSize", String.valueOf(criteria.getPageSize()));

		model.addAttribute("partnerList", service.getList(criteria));
		model.addAttribute("newPartnerCode", "");
		model.addAttribute("search", search);
		model.addAttribute("page", criteria.getPage());
		model.addAttribute("pageSize", criteria.getPageSize());
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("basePath", "/master/partner");
		model.addAttribute("activeMenu", "master");
		model.addAttribute("pageTitle", "取引先登録");
		model.addAttribute("pageScript", "master.js");
		model.addAttribute("pageStyle", "master.css");

		return "master/partner";
	}
}
