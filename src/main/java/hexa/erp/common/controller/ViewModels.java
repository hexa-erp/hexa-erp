package hexa.erp.common.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.Model;

/** 화면에 필요한 검색조건과 기본값을 만드는 도우미. */
public final class ViewModels {
	private ViewModels() {
	}

	public static String notImplemented(HttpServletResponse response, Model model) {
		response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
		model.addAttribute("errorMessage", "아직 구현되지 않은 기능입니다. 실제 데이터는 변경되지 않았습니다.");
		return "common/not-implemented";
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

	public static Map<String, List<String>> filters(HttpServletRequest request, Map<String, String> search, Model model,
			String... kinds) {
		Map<String, List<String>> filterIds = new LinkedHashMap<>();
		for (String kind : Arrays.asList("warehouse", "partner", "item", "assignee")) {
			String parameter = kind + "Ids";
			if (Arrays.asList(kinds).contains(kind)) {
				String[] values = request.getParameterValues(parameter);
				// 반복 ID가 없으면 단일 ID를 읽는다.
				if (values == null && search.get(kind + "Id") != null) {
					values = new String[] { search.get(kind + "Id") };
				}
				List<String> ids = new ArrayList<>();
				if (values != null) {
					for (String value : values) {
						String id = value == null ? "" : value.trim();
						if (id.isEmpty() || ids.contains(id)) {
							continue;
						}
						ids.add(id);
					}
				}
				filterIds.put(parameter, ids);
			}
			// 여러 ID로 된 조건은 search 대신 filterIds로 전달한다.
			search.remove(parameter);
			search.remove(kind + "Id");
			search.remove(kind + "Name");
		}
		model.addAttribute("filterIds", filterIds);
		return filterIds;
	}
}
