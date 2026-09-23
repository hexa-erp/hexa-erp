package hexa.erp.partner.controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hexa.erp.common.controller.ViewModels;

@Controller
@RequestMapping("/master/partner")
public class PartnerController {
	@GetMapping
	public String list(@RequestParam Map<String, String> params, Model model) {
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

	@PostMapping({ "/save", "/active" })
	@ResponseBody
	public ResponseEntity<Map<String, Object>> notImplemented() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("saved", false);
		body.put("message", "아직 구현되지 않은 기능입니다. 실제 데이터는 저장되지 않았습니다.");
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(body);
	}
}
