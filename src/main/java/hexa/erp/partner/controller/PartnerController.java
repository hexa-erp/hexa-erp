package hexa.erp.partner.controller;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hexa.erp.common.controller.ViewModels;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/master/partner")
public class PartnerController {
	@GetMapping
	public String list(@RequestParam Map<String, String> params, Model model) {
		log.info("거래처 목록 조회 요청");
		model.addAttribute("partnerList", Collections.emptyList());
		model.addAttribute("newPartnerCode", "");
		model.addAttribute("search", ViewModels.search(params));
		model.addAttribute("activeMenu", "master");
		model.addAttribute("pageTitle", "거래처 등록");
		model.addAttribute("pageScript", "master.js");
		model.addAttribute("pageStyle", "master.css");
		ViewModels.emptyPage(model, 50, "/master/partner");
		return "master/partner";
	}

	@PostMapping("/save")
	public String save(HttpServletResponse response, Model model) {
		log.info("거래처 저장 요청");
		return ViewModels.notImplemented(response, model);
	}

	@PostMapping("/active")
	public String changeActive(HttpServletResponse response, Model model) {
		log.info("거래처 사용 여부 변경 요청");
		return ViewModels.notImplemented(response, model);
	}

}
