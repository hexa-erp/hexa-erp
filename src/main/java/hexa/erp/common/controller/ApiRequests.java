package hexa.erp.common.controller;

import java.util.List;

import hexa.erp.common.domain.SelectedIdsVO;

public final class ApiRequests {
	private ApiRequests() {
	}

	public static List<Long> requireIds(SelectedIdsVO request) {
		if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
			throw new IllegalArgumentException("처리할 항목을 선택해 주세요.");
		}
		for (Long id : request.getIds()) {
			if (id == null || id <= 0) {
				throw new IllegalArgumentException("선택한 항목의 식별자를 확인해 주세요.");
			}
		}
		return request.getIds();
	}
}
