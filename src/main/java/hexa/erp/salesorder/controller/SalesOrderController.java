package hexa.erp.salesorder.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
import hexa.erp.common.service.FilterSelectionService;
import hexa.erp.salesorder.domain.SalesOrderVO;
import hexa.erp.salesorder.service.SalesOrderService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/order")
public class SalesOrderController {
	@Setter(onMethod_ = @Autowired)
	private FilterSelectionService filterSelectionService;
	
	@Setter(onMethod_ = @Autowired)
	private SalesOrderService service;

	@GetMapping("/list")
	public String list(@RequestParam Map<String, String> params, Model model) {
		log.info("주문서 목록 조회 요청");
		model.addAttribute("orderList", Collections.emptyList());
		model.addAttribute("search", ViewModels.search(params));
		model.addAttribute("activeMenu", "order");
		model.addAttribute("pageTitle", "주문서 조회");
		ViewModels.emptyPage(model, 25, "/order/list");
		return "order/list";
	}

	//주문서 신규 입력 및 기존 주문서 조회
	@GetMapping("/form")
	public String form(@RequestParam(value = "id", required = false) Long id, @RequestParam Map<String, String> params, @RequestParam(value = "salesOrderId", required = false) Long salesOrderId, 
			HttpServletRequest request, Model model, RedirectAttributes rttr) {
		log.info("주문서 입력 화면 요청");
		
		Long documentId = id == null? salesOrderId : id;
		SalesOrderVO form;
		
		//신규 주문서 입력 시 기본값 설정
		if (documentId == null) {
			form = new SalesOrderVO();
			
			form.setBusinessDate(LocalDate.now().toString());
			form.setProgressStatus("IN_PROGRESS");
		}
		else {
			//기존 주문서 조회
			form = service.get(documentId);
			if (form == null) {
				rttr.addFlashAttribute("errorMessage", "조회할 주문서가 없습니다.");
				
				return PostRedirects.toList("/order/list", request);
			}
		}
		
		model.addAttribute("form", form);
		model.addAttribute("mode", "create");
		model.addAttribute("isEdit", false);
		model.addAttribute("activeMenu", "order");
		model.addAttribute("pageTitle", "주문서 입력");
		return "order/form";
	}

	@GetMapping("/status")
	public String status(@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("주문서 현황 조회 요청");
		Map<String, String> search = ViewModels.search(params);
		// 현황은 검색 결과 전체를 표시하며 페이지 분할하지 않는다.
		search.remove("page");
		search.remove("pageSize");
		Map<String, List<String>> filterIds = ViewModels.filters(request, search, model, "warehouse", "partner", "item",
				"assignee");
		model.addAttribute("filterSelections", filterSelectionService.getSelections(filterIds));
		model.addAttribute("search", search);
		model.addAttribute("monthGroups", Collections.emptyList());
		model.addAttribute("statusTotals", Collections.emptyMap());
		model.addAttribute("activeMenu", "order");
		model.addAttribute("pageTitle", "주문서 현황");
		return "order/status";
	}

	@GetMapping("/statement")
	public String statement(Model model) {
		log.info("주문서 전표 조회 요청");
		model.addAttribute("form", Collections.singletonMap("lines", Collections.emptyList()));
		model.addAttribute("statementTotals", Collections.emptyMap());
		model.addAttribute("activeMenu", "order");
		model.addAttribute("pageTitle", "주문서 전표");
		return "order/statement";
	}

	@GetMapping("/unsold")
	public String unsold(@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("미판매 현황 조회 요청");
		Map<String, String> search = ViewModels.search(params);
		search.remove("page");
		search.remove("pageSize");
		if (search.get("cutoffDate") == null || search.get("cutoffDate").trim().isEmpty()) {
			search.put("cutoffDate", LocalDate.now().toString());
		}
		Map<String, List<String>> filterIds = ViewModels.filters(request, search, model, "warehouse", "partner", "item",
				"assignee");
		model.addAttribute("filterSelections", filterSelectionService.getSelections(filterIds));
		model.addAttribute("search", search);
		model.addAttribute("monthGroups", Collections.emptyList());
		model.addAttribute("remainingTotals", Collections.emptyMap());
		model.addAttribute("activeMenu", "order");
		model.addAttribute("pageTitle", "미판매 현황");
		return "order/unsold";
	}

	//주문서 신규 등록 및 수정
	@PostMapping("/save")
	public String save(SalesOrderVO salesOrder, BindingResult bindingResult, HttpServletRequest request, RedirectAttributes rttr) {
		log.info("견적서 저장 요청");
		
		//신규 등록 및 수정 구분
		boolean editing = salesOrder.getSalesOrderId() != null;
		
		if (bindingResult.hasErrors()) {
			rttr.addFlashAttribute("errorMessage", "품목의 수량량과 단가 등을 입력해 주세요.");
		}
		else {
			try {
				//주문서 저장
				service.save(salesOrder);
			} catch(IllegalArgumentException e) {
				rttr.addFlashAttribute("errorMessage", e.getMessage());
			} 
			catch(DataAccessException e){
				log.error("주문서 저장 실패", e);
				rttr.addFlashAttribute("errorMessage","주문서를 저장하지 못했습니다.");
			}
		}
		
		return PostRedirects.afterDocumentSave("/order", editing, request);
	}
	

	@PostMapping("/delete")
	public String delete(HttpServletResponse response, Model model) {
		log.info("주문서 삭제 요청");
		return ViewModels.notImplemented(response, model);
	}

	@PostMapping("/change-status")
	public String changeStatus(HttpServletResponse response, Model model) {
		log.info("주문서 진행 상태 변경 요청");
		return ViewModels.notImplemented(response, model);
	}

}
