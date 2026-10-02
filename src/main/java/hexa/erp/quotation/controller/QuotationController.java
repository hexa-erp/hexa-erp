package hexa.erp.quotation.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
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
import hexa.erp.quotation.domain.QuotationVO;
import hexa.erp.quotation.service.QuotationService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/quotation")
public class QuotationController {
	@Setter(onMethod_ = @Autowired)
	private FilterSelectionService filterSelectionService;
	
	@Setter(onMethod_ = @Autowired)
	private QuotationService service;

	@GetMapping("/list")
	public String list(@RequestParam Map<String, String> params, Model model) {
		log.info("견적서 목록 조회 요청");
		model.addAttribute("quotationList", Collections.emptyList());
		model.addAttribute("search", ViewModels.search(params));
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "견적서 조회");
		ViewModels.emptyPage(model, 25, "/quotation/list");
		return "quotation/list";
	}

	@GetMapping("/form")
	public String form(@RequestParam(value = "id", required = false) Long id,@RequestParam(value = "quotationId", required = false) Long quotationId, 
			HttpServletRequest request, RedirectAttributes rttr, Model model) {
		log.info("견적서 입력 화면 요청");
	
		Long documentId = id == null? quotationId : id;
		QuotationVO form;
		
		if (documentId == null) {
			form = new QuotationVO();
			
			form.setBusinessDate(LocalDate.now().toString());
			form.setProgressStatus("IN_PROGRESS");
		}
		else {
			form = service.get(quotationId);
			if (form == null) {
				rttr.addFlashAttribute("erroMessage", "조회할 견적서가 없습니다.");
				return PostRedirects.toList("/quotation/list", request);
			}
		}
		
		model.addAttribute("form", form);
		model.addAttribute("mode", "create");
		model.addAttribute("isEdit", false);
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "견적서 입력");
		return "quotation/form";
	}

	@GetMapping("/status")
	public String status(@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("견적서 현황 조회 요청");
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
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "견적서 현황");
		return "quotation/status";
	}

	@GetMapping("/statement")
	public String statement(Model model) {
		log.info("견적서 전표 조회 요청");
		model.addAttribute("form", Collections.singletonMap("lines", Collections.emptyList()));
		model.addAttribute("statementTotals", Collections.emptyMap());
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "견적서 전표");
		return "quotation/statement";
	}

	@GetMapping("/unordered")
	public String unordered(@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("미주문 현황 조회 요청");
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
		model.addAttribute("activeMenu", "quotation");
		model.addAttribute("pageTitle", "미주문 현황");
		return "quotation/unordered";
	}

	@PostMapping("/save")
	public String save(QuotationVO quotation, BindingResult bindingResult,
						HttpServletRequest request, RedirectAttributes rttr) {
		log.info("견적서 저장 요청");
		
		boolean editing = quotation.getQuotationId() != null;
		
		if (bindingResult.hasErrors()) {
			rttr.addFlashAttribute("errorMessage", "품목의 수량량과 단가 등을 입력해 주세요.");
		}
		else {
			try {
				service.save(quotation);
			} catch(IllegalArgumentException e) {
				rttr.addFlashAttribute("errorMessage", e.getMessage());
			} 
			catch(DataAccessException e){
				log.error("견적서 저장 실패", e);
				rttr.addFlashAttribute("errorMessage","견적서를 저장하지 못했습니다.");
			}
		}
		return PostRedirects.afterDocumentSave("/quotation", editing, request);
	}

	@PostMapping("/delete")
	public String delete(HttpServletResponse response, Model model) {
		log.info("견적서 삭제 요청");
		return ViewModels.notImplemented(response, model);
	}

	@PostMapping("/change-status")
	public String changeStatus(HttpServletResponse response, Model model) {
		log.info("견적서 진행 상태 변경 요청");
		return ViewModels.notImplemented(response, model);
	}

}
