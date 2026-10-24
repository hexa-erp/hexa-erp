package hexa.erp.shipinstruction.controller;

import java.time.LocalDate;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hexa.erp.common.controller.PostRedirects;
import hexa.erp.common.controller.ViewModels;
import hexa.erp.shipinstruction.domain.ShipInstructionCriteria;
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
	public String list(ShipInstructionCriteria criteria, @RequestParam Map<String, String> params, Model model) {
		log.info("출하지시서 목록 조회 요청");
		int totalCount = service.getTotal(criteria);
		int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / criteria.getPageSize()));
		criteria.setPage(Math.min(criteria.getPage(), totalPages));
		Map<String, String> search = ViewModels.search(params);
		search.put("keyword", criteria.getKeyword());
		search.put("progressStatus", criteria.getProgressStatus());
		search.put("page", String.valueOf(criteria.getPage()));
		search.put("pageSize", String.valueOf(criteria.getPageSize()));

		model.addAttribute("shipInstructionList", service.getList(criteria));
		model.addAttribute("search", search);
		model.addAttribute("page", criteria.getPage());
		model.addAttribute("pageSize", criteria.getPageSize());
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("basePath", "/shipping-instruction/list");
		model.addAttribute("activeMenu", "shipping-instruction");
		model.addAttribute("pageTitle", "출하지시서 조회");
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
}
