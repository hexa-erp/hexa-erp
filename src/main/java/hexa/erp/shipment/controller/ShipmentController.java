package hexa.erp.shipment.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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

	@PostMapping("/save")
	public String save(ShipmentVO shipment, BindingResult bindingResult, HttpServletRequest request,
			RedirectAttributes rttr) {
		log.info("출하 저장 요청");
		boolean editing = shipment.getShipmentId() != null;
		if (bindingResult.hasErrors()) {
			rttr.addFlashAttribute("errorMessage", "품목의 수량 등 입력값을 확인해 주세요.");
		} else {
			try {
				service.save(shipment);
			} catch (IllegalArgumentException e) {
				rttr.addFlashAttribute("errorMessage", e.getMessage());
			} catch (DataAccessException e) {
				log.error("출하 저장 실패", e);
				rttr.addFlashAttribute("errorMessage", "출하를 저장하지 못했습니다. 입력 내용을 확인해 주세요.");
			}
		}
		return PostRedirects.afterDocumentSave("/shipment", editing, request);
	}

	@PostMapping("/delete")
	public String delete(@RequestParam(value = "selectedIds", required = false) List<Long> selectedIds,
			HttpServletRequest request, RedirectAttributes rttr) {
		log.info("출하 삭제 요청");
		if (selectedIds == null || selectedIds.isEmpty()) {
			rttr.addFlashAttribute("errorMessage", "삭제할 출하를 선택해 주세요.");
		} else {
			try {
				if (service.remove(selectedIds) == 0) {
					rttr.addFlashAttribute("errorMessage", "삭제할 출하가 없습니다.");
				}
			} catch (DataAccessException e) {
				log.error("출하 삭제 실패", e);
				rttr.addFlashAttribute("errorMessage", "출하를 삭제하지 못했습니다.");
			}
		}
		return PostRedirects.toList("/shipment/list", request);
	}

	@PostMapping("/change-status")
	public String changeStatus(@RequestParam(value = "selectedIds", required = false) List<Long> selectedIds,
			@RequestParam(value = "nextProgressStatus", defaultValue = "") String nextProgressStatus,
			HttpServletRequest request, RedirectAttributes rttr) {
		log.info("출하 진행 상태 변경 요청");
		if (selectedIds == null || selectedIds.isEmpty()) {
			rttr.addFlashAttribute("errorMessage", "진행상태를 변경할 출하를 선택해 주세요.");
		} else {
			try {
				if (service.changeStatus(selectedIds, nextProgressStatus) == 0) {
					rttr.addFlashAttribute("errorMessage", "변경할 출하가 없습니다.");
				}
			} catch (IllegalArgumentException e) {
				rttr.addFlashAttribute("errorMessage", e.getMessage());
			} catch (DataAccessException e) {
				log.error("출하 진행 상태 변경 실패", e);
				rttr.addFlashAttribute("errorMessage", "출하의 진행상태를 변경하지 못했습니다.");
			}
		}
		return PostRedirects.toList("/shipment/list", request);
	}
}