package hexa.erp.common.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CommonViewAdvice {
	@ModelAttribute
	public void common(Model model, HttpServletRequest request) {
		// 배너 표시와 저장 허용은 별개다. 저장 차단은 Form의 data-unimplemented-submit으로 정한다.
		model.addAttribute("developmentMode", true);
		model.addAttribute("currentPath", request.getServletPath());
		model.addAttribute("navigation", navigation());
		model.addAttribute("connectedAt", new Date(request.getSession().getCreationTime()));
	}

	private List<Map<String, Object>> navigation() {
		List<Map<String, Object>> groups = new ArrayList<>();
		groups.add(group("master", "기초등록", "거래처 등록", "/master/partner", "창고 등록", "/master/warehouse", "품목 등록",
				"/master/item"));
		groups.add(group("quotation", "견적서", "견적서 조회", "/quotation/list", "견적서 입력", "/quotation/form", "견적서 현황",
				"/quotation/status", "미주문 현황", "/quotation/unordered"));
		groups.add(group("order", "주문서", "주문서 조회", "/order/list", "주문서 입력", "/order/form", "주문서 현황", "/order/status",
				"미판매 현황", "/order/unsold"));
		groups.add(group("sale", "판매", "판매 조회", "/sale/list", "판매 입력", "/sale/form", "판매 단가 일괄 변경", "/sale/bulk-price",
				"판매 현황", "/sale/status"));
		groups.add(group("shipping-instruction", "출하지시서", "출하지시서 조회", "/shipping-instruction/list", "출하지시서 입력",
				"/shipping-instruction/form"));
		groups.add(group("shipment", "출하", "출하 조회", "/shipment/list", "출하 입력", "/shipment/form"));
		return groups;
	}

	private Map<String, Object> group(String key, String label, String... entries) {
		List<Map<String, Object>> links = new ArrayList<>();
		for (int i = 0; i < entries.length; i += 2)
			links.add(map("label", entries[i], "path", entries[i + 1]));
		return map("key", key, "label", label, "entries", links);
	}

	private Map<String, Object> map(Object... pairs) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (int i = 0; i < pairs.length; i += 2)
			result.put((String) pairs[i], pairs[i + 1]);
		return result;
	}
}
