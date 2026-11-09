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
		// 画面上部に開発用の案内を表示する。
		model.addAttribute("developmentMode", true);
		model.addAttribute("currentPath", request.getServletPath());
		model.addAttribute("returnSearch", PostRedirects.returnSearch(request));
		model.addAttribute("navigation", navigation());
		model.addAttribute("connectedAt", new Date(request.getSession().getCreationTime()));
	}

	private List<Map<String, Object>> navigation() {
		List<Map<String, Object>> groups = new ArrayList<>();
		groups.add(group("master", "マスター登録", "取引先登録", "/master/partner", "倉庫登録", "/master/warehouse", "品目登録",
				"/master/item"));
		groups.add(group("quotation", "見積書", "見積一覧", "/quotation/list", "見積入力", "/quotation/form", "見積集計",
				"/quotation/status", "未受注一覧", "/quotation/unordered"));
		groups.add(group("order", "受注伝票", "受注一覧", "/order/list", "受注入力", "/order/form", "受注集計", "/order/status",
				"未売上一覧", "/order/unsold"));
		groups.add(group("sale", "売上", "売上一覧", "/sale/list", "売上入力", "/sale/form", "売上単価一括変更", "/sale/bulk-price",
				"売上集計", "/sale/status"));
		groups.add(group("shipping-instruction", "出荷指示書", "出荷指示一覧", "/shipping-instruction/list", "出荷指示入力",
				"/shipping-instruction/form"));
		groups.add(group("shipment", "出荷", "出荷一覧", "/shipment/list", "出荷入力", "/shipment/form"));
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
