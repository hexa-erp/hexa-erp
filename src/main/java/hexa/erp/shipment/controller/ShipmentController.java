package hexa.erp.shipment.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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
@RequestMapping("/shipment")
public class ShipmentController {
	@GetMapping("/list")
	public String list(@RequestParam Map<String, String> params, Model model) {
		model.addAttribute("shipmentList", Collections.emptyList());
		model.addAttribute("search", ViewModels.search(params));
		model.addAttribute("activeMenu", "shipment");
		model.addAttribute("pageTitle", "출하 조회");
		ViewModels.emptyPage(model, 25, "/shipment/list");
		return "shipment/list";
	}

	@GetMapping("/form")
	public String form(@RequestParam Map<String, String> params, HttpServletResponse response, Model model) {
		// 수정 조회 미구현 상태에서 요청 ID가 신규 저장 ID로 사용되지 않게 한다.
		if ((params.get("id") != null && !params.get("id").trim().isEmpty())
				|| (params.get("shipmentId") != null && !params.get("shipmentId").trim().isEmpty())) {
			response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
		Map<String, Object> form = new LinkedHashMap<>();
		form.put("shipmentId", "");
		form.put("shipmentNo", "");
		form.put("businessDate", LocalDate.now().toString());
		form.put("progressStatus", "CONFIRMED");
		form.put("lines", Collections.emptyList());
		model.addAttribute("form", form);
		model.addAttribute("mode", "create");
		model.addAttribute("isEdit", false);
		model.addAttribute("activeMenu", "shipment");
		model.addAttribute("pageTitle", "출하 입력");
		return "shipment/form";
	}

	@PostMapping({ "/save", "/delete", "/change-status" })
	@ResponseBody
	public ResponseEntity<Map<String, Object>> notImplemented() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("saved", false);
		body.put("message", "아직 구현되지 않은 기능입니다. 실제 데이터는 저장되지 않았습니다.");
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(body);
	}
}
