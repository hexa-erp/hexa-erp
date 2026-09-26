package hexa.erp.quotation.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hexa.erp.common.controller.ViewModels;
import hexa.erp.common.service.FilterSelectionService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/quotation")
public class QuotationController {
	@Setter(onMethod_ = @Autowired)
	private FilterSelectionService filterSelectionService;

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
	public String form(@RequestParam Map<String, String> params, HttpServletResponse response, Model model) {
		log.info("견적서 입력 화면 요청");
		// TODO: 수정할 전표와 품목 내역을 조회해 form에 담는다.
		if ((params.get("id") != null && !params.get("id").trim().isEmpty())
				|| (params.get("quotationId") != null && !params.get("quotationId").trim().isEmpty())) {
			response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
			model.addAttribute("errorMessage", "기존 전표 조회는 아직 구현되지 않았습니다.");
		}
		Map<String, Object> form = new LinkedHashMap<>();
		form.put("quotationId", "");
		form.put("quotationNo", "");
		form.put("businessDate", LocalDate.now().toString());
		form.put("progressStatus", "IN_PROGRESS");
		form.put("lines", Collections.emptyList());
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
	public String save(HttpServletResponse response, Model model) {
		log.info("견적서 저장 요청");
		return ViewModels.notImplemented(response, model);
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
