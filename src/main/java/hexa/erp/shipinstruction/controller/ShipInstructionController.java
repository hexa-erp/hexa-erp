package hexa.erp.shipinstruction.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
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
@RequestMapping("/shipping-instruction")
public class ShipInstructionController {
	@GetMapping("/list")
	public String list(@RequestParam Map<String, String> params, Model model) {
		log.info("출하지시서 목록 조회 요청");
		model.addAttribute("shipInstructionList", Collections.emptyList());
		model.addAttribute("search", ViewModels.search(params));
		model.addAttribute("activeMenu", "shipping-instruction");
		model.addAttribute("pageTitle", "출하지시서 조회");
		ViewModels.emptyPage(model, 25, "/shipping-instruction/list");
		return "shippingInstruction/list";
	}

	@GetMapping("/form")
	public String form(@RequestParam Map<String, String> params, HttpServletResponse response, Model model) {
		log.info("출하지시서 입력 화면 요청");
		// TODO: 수정할 전표와 품목 내역을 조회해 form에 담는다.
		if ((params.get("id") != null && !params.get("id").trim().isEmpty())
				|| (params.get("shipInstructionId") != null && !params.get("shipInstructionId").trim().isEmpty())) {
			response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
			model.addAttribute("errorMessage", "기존 전표 조회는 아직 구현되지 않았습니다.");
		}
		Map<String, Object> form = new LinkedHashMap<>();
		form.put("shipInstructionId", "");
		form.put("shipInstructionNo", "");
		form.put("businessDate", LocalDate.now().toString());
		form.put("progressStatus", "IN_PROGRESS");
		form.put("lines", Collections.emptyList());
		model.addAttribute("form", form);
		model.addAttribute("mode", "create");
		model.addAttribute("isEdit", false);
		model.addAttribute("activeMenu", "shipping-instruction");
		model.addAttribute("pageTitle", "출하지시서 입력");
		return "shippingInstruction/form";
	}

	@PostMapping("/save")
	public String save(HttpServletResponse response, Model model) {
		log.info("출하지시서 저장 요청");
		return ViewModels.notImplemented(response, model);
	}

	@PostMapping("/delete")
	public String delete(HttpServletResponse response, Model model) {
		log.info("출하지시서 삭제 요청");
		return ViewModels.notImplemented(response, model);
	}

	@PostMapping("/change-status")
	public String changeStatus(HttpServletResponse response, Model model) {
		log.info("출하지시서 진행 상태 변경 요청");
		return ViewModels.notImplemented(response, model);
	}

}
