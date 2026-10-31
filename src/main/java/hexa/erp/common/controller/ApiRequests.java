package hexa.erp.common.controller;

import java.util.List;

import hexa.erp.common.domain.SelectedIdsVO;

public final class ApiRequests {
	private ApiRequests() {
	}

	// 一括処理で対象が未選択、または無効なIDが含まれる場合は、処理を開始せず案内する。
	public static List<Long> requireIds(SelectedIdsVO request) {
		if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
			throw new IllegalArgumentException("処理する項目を選択してください。");
		}
		for (Long id : request.getIds()) {
			if (id == null || id <= 0) {
				throw new IllegalArgumentException("選択した項目のIDを確認してください。");
			}
		}
		return request.getIds();
	}
}
