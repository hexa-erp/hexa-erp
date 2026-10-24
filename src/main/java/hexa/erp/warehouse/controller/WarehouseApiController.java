package hexa.erp.warehouse.controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hexa.erp.common.controller.ApiException;
import hexa.erp.common.controller.ApiRequests;
import hexa.erp.common.domain.ActiveChangeVO;
import hexa.erp.common.domain.ApiResponseVO;
import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/warehouses", produces = MediaType.APPLICATION_JSON_VALUE)
public class WarehouseApiController {

	private final WarehouseService service;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> create(@RequestBody WarehouseVO warehouse) {
		if (warehouse.getWarehouseId() != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "新規倉庫にIDを指定することはできません。");
		}
		validate(warehouse, true);
		warehouse.setWarehouseCode(warehouse.getWarehouseCode().trim());
		try {
			service.register(warehouse);
		} catch (DuplicateKeyException e) {
			throw new ApiException(HttpStatus.CONFLICT, "登録済みの倉庫コードです。別のコードを入力してください。");
		}
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponseVO("倉庫を登録しました。", warehouse.getWarehouseId(), null));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseVO update(@PathVariable("id") Long id, @RequestBody WarehouseVO warehouse) {
		warehouse.setWarehouseId(id);
		validate(warehouse, false);
		if (!service.modify(warehouse)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "更新対象の倉庫がありません。");
		}
		return new ApiResponseVO("倉庫を更新しました。", id, null);
	}

	@PatchMapping(value = "/active", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseVO changeActive(@RequestBody ActiveChangeVO change) {
		ApiRequests.requireIds(change);
		if (!"Y".equals(change.getActiveFlag()) && !"N".equals(change.getActiveFlag())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "利用状態を確認してください。");
		}
		int count = service.changeActive(change.getIds(), change.getActiveFlag());
		if (count == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "変更対象の倉庫がありません。");
		}
		return new ApiResponseVO("倉庫の利用状態を変更しました。", null, count);
	}

	private void validate(WarehouseVO warehouse, boolean creating) {
		if (!StringUtils.hasText(warehouse.getWarehouseName())
				|| (creating && !StringUtils.hasText(warehouse.getWarehouseCode()))) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "倉庫コードと倉庫名を入力してください。");
		}
		if (!"倉庫".equals(warehouse.getWarehouseType()) && !"工場".equals(warehouse.getWarehouseType())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "倉庫区分を選択してください。");
		}
		warehouse.setWarehouseName(warehouse.getWarehouseName().trim());
	}
}
