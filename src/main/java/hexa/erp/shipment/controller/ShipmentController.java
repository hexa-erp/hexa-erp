package hexa.erp.shipment.controller;

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
import hexa.erp.shipment.domain.ShipmentCriteria;
import hexa.erp.shipment.domain.ShipmentVO;
import hexa.erp.shipment.service.ShipmentService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/shipment")
public class ShipmentController {
	@Setter(onMethod_ = @Autowired)
	private ShipmentService service;

	@GetMapping("/list")
	public String list(ShipmentCriteria criteria, @RequestParam Map<String, String> params, Model model) {
		log.info("출하 목록 조회 요청");
		int totalCount = service.getTotal(criteria);
		int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / criteria.getPageSize()));
		// 삭제 등으로 페이지 수가 줄면 마지막 페이지를 보여준다.
		criteria.setPage(Math.min(criteria.getPage(), totalPages));
		Map<String, String> search = ViewModels.search(params);
		search.put("keyword", criteria.getKeyword());
		search.put("progressStatus", criteria.getProgressStatus());
		search.put("page", String.valueOf(criteria.getPage()));
		search.put("pageSize", String.valueOf(criteria.getPageSize()));
		model.addAttribute("shipmentList", service.getList(criteria));
		model.addAttribute("search", search);
		model.addAttribute("page", criteria.getPage());
		model.addAttribute("pageSize", criteria.getPageSize());
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("basePath", "/shipment/list");
		model.addAttribute("activeMenu", "shipment");
		model.addAttribute("pageTitle", "출하 조회");
		return "shipment/list";
	}

	@GetMapping("/form")
	public String form(@RequestParam(value = "id", required = false) Long id,
			@RequestParam(value = "shipmentId", required = false) Long shipmentId, HttpServletRequest request,
			Model model, RedirectAttributes rttr) {
		log.info("출하 입력 화면 요청");
		// id와 shipmentId 중 하나라도 있으면 수정 화면이다.
		Long documentId = id == null ? shipmentId : id;
		ShipmentVO form;
		if (documentId == null) {
			form = new ShipmentVO();
			form.setBusinessDate(LocalDate.now().toString());
			form.setProgressStatus("CONFIRMED");
		} else {
			form = service.get(documentId);
			if (form == null) {
				rttr.addFlashAttribute("errorMessage", "조회할 출하가 없습니다.");
				return PostRedirects.toList("/shipment/list", request);
			}
		}
		model.addAttribute("form", form);
		model.addAttribute("mode", documentId == null ? "create" : "edit");
		model.addAttribute("isEdit", documentId != null);
		model.addAttribute("activeMenu", "shipment");
		model.addAttribute("pageTitle", "출하 입력");
		return "shipment/form";
	}
}
