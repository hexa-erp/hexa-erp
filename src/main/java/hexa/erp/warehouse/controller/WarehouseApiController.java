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
			throw new ApiException(HttpStatus.BAD_REQUEST, "신규 창고에는 식별자를 지정할 수 없습니다.");
		}
		validate(warehouse, true);
		warehouse.setWarehouseCode(warehouse.getWarehouseCode().trim());
		try {
			service.register(warehouse);
		} catch (DuplicateKeyException e) {
			throw new ApiException(HttpStatus.CONFLICT, "이미 등록된 창고 코드입니다. 다른 코드를 입력해 주세요.");
		}
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponseVO("창고를 등록했습니다.", warehouse.getWarehouseId(), null));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseVO update(@PathVariable("id") Long id, @RequestBody WarehouseVO warehouse) {
		warehouse.setWarehouseId(id);
		validate(warehouse, false);
		if (!service.modify(warehouse)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "수정할 창고가 없습니다.");
		}
		return new ApiResponseVO("창고를 수정했습니다.", id, null);
	}

	@PatchMapping(value = "/active", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseVO changeActive(@RequestBody ActiveChangeVO change) {
		ApiRequests.requireIds(change);
		if (!"Y".equals(change.getActiveFlag()) && !"N".equals(change.getActiveFlag())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "사용 여부를 확인해 주세요.");
		}
		int count = service.changeActive(change.getIds(), change.getActiveFlag());
		if (count == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "변경할 창고가 없습니다.");
		}
		return new ApiResponseVO("창고의 사용 여부를 변경했습니다.", null, count);
	}

	private void validate(WarehouseVO warehouse, boolean creating) {
		if (!StringUtils.hasText(warehouse.getWarehouseName())
				|| (creating && !StringUtils.hasText(warehouse.getWarehouseCode()))) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "창고 코드와 창고명을 입력해 주세요.");
		}
		if (!"창고".equals(warehouse.getWarehouseType()) && !"공장".equals(warehouse.getWarehouseType())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "창고 구분을 선택해 주세요.");
		}
		warehouse.setWarehouseName(warehouse.getWarehouseName().trim());
	}
}
