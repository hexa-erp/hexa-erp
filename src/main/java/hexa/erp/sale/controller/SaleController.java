package hexa.erp.sale.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
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
@RequestMapping("/sale")
public class SaleController {
	@GetMapping("/list")
	public String list(@RequestParam Map<String, String> params, Model model) {
		model.addAttribute("saleList", Collections.emptyList());
		model.addAttribute("search", ViewModels.search(params));
		model.addAttribute("activeMenu", "sale");
		model.addAttribute("pageTitle", "판매 조회");
		ViewModels.emptyPage(model, 25, "/sale/list");
		return "sale/list";
	}

	@GetMapping("/form")
	public String form(@RequestParam Map<String, String> params, HttpServletResponse response, Model model) {
		// 수정 조회 미구현 상태에서 요청 ID가 신규 저장 ID로 사용되지 않게 한다.
		if ((params.get("id") != null && !params.get("id").trim().isEmpty())
				|| (params.get("saleId") != null && !params.get("saleId").trim().isEmpty())) {
			response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
		Map<String, Object> form = new LinkedHashMap<>();
		form.put("saleId", "");
		form.put("saleNo", "");
		form.put("businessDate", LocalDate.now().toString());
		form.put("progressStatus", "CONFIRMED");
		form.put("lines", Collections.emptyList());
		model.addAttribute("form", form);
		model.addAttribute("mode", "create");
		model.addAttribute("isEdit", false);
		model.addAttribute("activeMenu", "sale");
		model.addAttribute("pageTitle", "판매 입력");
		return "sale/form";
	}

	@GetMapping("/status")
	public String status(@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		Map<String, String> search = ViewModels.search(params);
		// 현황은 검색 결과 전체를 표시하며 페이지 분할하지 않는다.
		search.remove("page");
		search.remove("pageSize");
		ViewModels.filters(request, search, model, "warehouse", "partner", "item", "assignee");
		model.addAttribute("search", search);
		model.addAttribute("saleStatusList", Collections.emptyList());
		model.addAttribute("statusList", Collections.emptyList());
		model.addAttribute("reportMonths", Collections.emptyList());
		model.addAttribute("statusTotals", Collections.emptyMap());
		model.addAttribute("activeMenu", "sale");
		model.addAttribute("pageTitle", "판매 현황");
		return "sale/status";
	}

	@GetMapping("/statement")
	public String statement(Model model) {
		model.addAttribute("form", Collections.singletonMap("lines", Collections.emptyList()));
		model.addAttribute("statementTotals", Collections.emptyMap());
		model.addAttribute("activeMenu", "sale");
		model.addAttribute("pageTitle", "거래명세서");
		return "sale/statement";
	}

	@GetMapping("/bulk-price")
	public String bulkPrice(@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		Map<String, String> search = ViewModels.search(params);
		// 담당자 표시 필드가 있어도 판매 단가 검색조건에는 포함하지 않는다.
		ViewModels.filters(request, search, model, "warehouse", "partner", "item");
		model.addAttribute("search", search);
		model.addAttribute("salePriceList", Collections.emptyList());
		model.addAttribute("activeMenu", "sale");
		model.addAttribute("pageTitle", "판매 단가 일괄 변경");
		model.addAttribute("pageScript", "sale.js");
		ViewModels.emptyPage(model, 25, "/sale/bulk-price");
		return "sale/bulk-price";
	}

	@PostMapping({ "/save", "/delete", "/change-status", "/bulk-price/save" })
	@ResponseBody
	public ResponseEntity<Map<String, Object>> notImplemented() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("saved", false);
		body.put("message", "아직 구현되지 않은 기능입니다. 실제 데이터는 저장되지 않았습니다.");
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(body);
	}
}
