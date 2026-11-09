package hexa.erp.common.controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 空の選択候補レスポンスを作成する。 */
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
