package hexa.erp.common.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public final class PostRedirects {

	private static final List<String> SEARCH_KEYS = Arrays.asList("keyword", "progressStatus", "includeInactive",
			"page", "pageSize", "datePreset", "startDate", "endDate", "view", "warehouseIds", "partnerIds", "itemIds",
			"assigneeIds", "warehouseId", "partnerId", "itemId", "assigneeId");

	private PostRedirects() {
	}

	public static Map<String, List<String>> returnSearch(HttpServletRequest request) {
		Map<String, List<String>> search = new LinkedHashMap<>();
		String path = request.getServletPath();
		boolean listRequest = "GET".equals(request.getMethod())
				&& (path.endsWith("/list") || path.endsWith("/bulk-price") || path.equals("/master/partner")
						|| path.equals("/master/warehouse") || path.equals("/master/item"));
		String prefix = listRequest ? "" : "return.";
		for (String key : SEARCH_KEYS) {
			String[] values = request.getParameterValues(prefix + key);
			if (values != null) {
				search.put(key, Arrays.asList(values));
			}
		}
		search.putIfAbsent("page", Collections.singletonList("1"));
		search.putIfAbsent("pageSize", Collections.singletonList(path.startsWith("/master/") ? "50" : "25"));
		return search;
	}

	public static String afterDocumentSave(String documentPath, boolean editing, HttpServletRequest request) {
		return editing ? toList(documentPath + "/list", request) : "redirect:" + documentPath + "/form";
	}

	public static String toList(String listPath, HttpServletRequest request) {
		return redirect(listPath, returnSearch(request));
	}

	public static String afterPriceChange(HttpServletRequest request) {
		Map<String, List<String>> search = returnSearch(request);
		search.put("view", Collections.singletonList("results"));
		return redirect("/sale/bulk-price", search);
	}

	private static String redirect(String path, Map<String, List<String>> search) {
		StringBuilder url = new StringBuilder("redirect:").append(path);
		String separator = "?";
		for (Map.Entry<String, List<String>> entry : search.entrySet()) {
			for (String value : entry.getValue()) {
				url.append(separator).append(encode(entry.getKey())).append('=').append(encode(value));
				separator = "&";
			}
		}
		return url.toString();
	}

	private static String encode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
}
