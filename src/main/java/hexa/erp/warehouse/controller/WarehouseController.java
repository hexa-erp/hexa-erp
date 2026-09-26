package hexa.erp.warehouse.controller;

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
@RequestMapping("/master/warehouse")
public class WarehouseController {
	@GetMapping
	public String list(@RequestParam Map<String, String> params, Model model) {
		log.info("창고 목록 조회 요청");
		model.addAttribute("warehouseList", Collections.emptyList());
		model.addAttribute("newWarehouseCode", "");
		model.addAttribute("search", ViewModels.search(params));
		model.addAttribute("activeMenu", "master");
		model.addAttribute("pageTitle", "창고 등록");
		model.addAttribute("pageScript", "master.js");
		model.addAttribute("pageStyle", "master.css");
		ViewModels.emptyPage(model, 50, "/master/warehouse");
		return "master/warehouse";
	}

	@PostMapping("/save")
	public String save(HttpServletResponse response, Model model) {
		log.info("창고 저장 요청");
		return ViewModels.notImplemented(response, model);
	}

	@PostMapping("/active")
	public String changeActive(HttpServletResponse response, Model model) {
		log.info("창고 사용 여부 변경 요청");
		return ViewModels.notImplemented(response, model);
	}

}
