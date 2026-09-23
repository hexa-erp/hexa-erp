package hexa.erp.common.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

/** 빈 화면 기본값과 요청 조건만 전달하며 업무 조회는 하지 않는다. */
public final class ViewModels {
	private ViewModels() {
	}

	public static Map<String, String> search(Map<String, String> params) {
		Map<String, String> search = new LinkedHashMap<>(params);
		search.putIfAbsent("keyword", "");
		search.putIfAbsent("progressStatus", "");
		search.putIfAbsent("datePreset", "custom");
		return search;
	}

	public static void emptyPage(Model model, int pageSize, String basePath) {
		model.addAttribute("page", 1);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("totalPages", 1);
		model.addAttribute("totalCount", 0);
		model.addAttribute("basePath", basePath);
	}

	/** 태그의 "ID ..."는 요청 조건이며 실제 이름·CODE가 아니다. 화면에 없는 필터는 제외한다. */
	public static void filters(HttpServletRequest request, Map<String, String> search, Model model, String... kinds) {
		Map<String, List<String>> filterIds = new LinkedHashMap<>();
		Map<String, List<Map<String, String>>> selections = new LinkedHashMap<>();
		for (String kind : Arrays.asList("warehouse", "partner", "item", "assignee")) {
			String parameter = kind + "Ids";
			if (Arrays.asList(kinds).contains(kind)) {
				String[] values = request.getParameterValues(parameter);
				// 기존 단일 ID 링크도 읽되, 명시적인 반복 파라미터가 우선한다.
				if (values == null && search.get(kind + "Id") != null) {
					values = new String[] { search.get(kind + "Id") };
				}
				List<String> ids = new ArrayList<>();
				List<Map<String, String>> selected = new ArrayList<>();
				if (values != null) {
					for (String value : values) {
						String id = value == null ? "" : value.trim();
						if (id.isEmpty() || ids.contains(id)) {
							continue;
						}
						ids.add(id);
						Map<String, String> tag = new LinkedHashMap<>();
						tag.put("id", id);
						tag.put("code", "");
						tag.put("name", "ID " + id);
						selected.add(tag);
					}
				}
				filterIds.put(parameter, ids);
				selections.put(kind, selected);
			}
			// 단일값 search와 반복값 filterIds가 링크·hidden에 중복 출력되지 않게 분리한다.
			search.remove(parameter);
			search.remove(kind + "Id");
			search.remove(kind + "Name");
		}
		model.addAttribute("filterIds", filterIds);
		model.addAttribute("filterSelections", selections);
	}
}
