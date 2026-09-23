package hexa.erp.common.controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 미구현 선택 목록용 응답이며 DB 오류의 대체 응답으로 사용하지 않는다. */
public final class LookupResponses {
	private LookupResponses() {
	}

	public static Map<String, Object> emptyResult() {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("rows", Collections.emptyList());
		result.put("page", 1);
		result.put("totalPages", 1);
		result.put("totalCount", 0);
		result.put("pageSize", 25);
		return result;
	}
}
