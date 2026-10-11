package hexa.erp.shipinstruction.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hexa.erp.common.controller.PostRedirects;
import hexa.erp.common.controller.ViewModels;
import hexa.erp.shipinstruction.domain.ShipInstructionVO;
import hexa.erp.shipinstruction.service.ShipInstructionService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/shipping-instruction")
public class ShipInstructionController {
	@Setter(onMethod_ = @Autowired)
	private ShipInstructionService service;

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
	public String form(@RequestParam(value = "id", required = false) Long id,
			@RequestParam(value = "shipInstructionId", required = false) Long shipInstructionId,
			HttpServletRequest request, Model model, RedirectAttributes rttr) {
		log.info("출하지시서 입력 화면 요청");
		Long documentId = id == null ? shipInstructionId : id;
		ShipInstructionVO form;

		if (documentId == null) {
			form = new ShipInstructionVO();
			form.setBusinessDate(LocalDate.now().toString());
			form.setProgressStatus("IN_PROGRESS");
		} else {
			form = service.get(documentId);
			if (form == null) {
				rttr.addFlashAttribute("errorMessage", "조회할 출하지시서가 없습니다.");
				return PostRedirects.toList("/shipping-instruction/list", request);
			}
		}
		model.addAttribute("form", form);
		model.addAttribute("mode", documentId == null ? "create" : "edit");
		model.addAttribute("isEdit", documentId != null);
		model.addAttribute("activeMenu", "shipping-instruction");
		model.addAttribute("pageTitle", "출하지시서 입력");
		return "shippingInstruction/form";
	}

	@PostMapping("/save")
	public String save(ShipInstructionVO shipInstruction, BindingResult bindingResult, HttpServletRequest request,
			RedirectAttributes rttr) {
		log.info("출하지시서 저장 요청");
		boolean editing = shipInstruction.getShipInstructionId() != null;
		if (bindingResult.hasErrors()) {
			rttr.addFlashAttribute("errorMessage", "품목의 수량 등 입력값을 확인해 주세요.");
		} else {
			try {
				service.save(shipInstruction);
			} catch (IllegalArgumentException e) {
				rttr.addFlashAttribute("errorMessage", e.getMessage());
			} catch (DataAccessException e) {
				log.error("출하지시서 저장 실패", e);
				rttr.addFlashAttribute("errorMessage", "출하지시서를 저장하지 못했습니다. 입력 내용을 확인해 주세요.");
			}
		}

		return PostRedirects.afterDocumentSave("/shipping-instruction", editing, request);
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
