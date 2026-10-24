package hexa.erp.partner.controller;

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
import hexa.erp.partner.domain.PartnerVO;
import hexa.erp.partner.service.PartnerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/partners", produces = MediaType.APPLICATION_JSON_VALUE)
public class PartnerApiController {

	private final PartnerService service;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> create(@RequestBody PartnerVO partner) {
		if (partner.getPartnerId() != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "신규 거래처에는 식별자를 지정할 수 없습니다.");
		}
		validate(partner, true);
		partner.setPartnerCode(partner.getPartnerCode().trim());
		try {
			service.register(partner);
		} catch (DuplicateKeyException e) {
			throw new ApiException(HttpStatus.CONFLICT, "이미 등록된 거래처 코드입니다. 다른 코드를 입력해 주세요.");
		}
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponseVO("거래처를 등록했습니다.", partner.getPartnerId(), null));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseVO update(@PathVariable("id") Long id, @RequestBody PartnerVO partner) {
		partner.setPartnerId(id);
		validate(partner, false);
		if (!service.modify(partner)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "수정할 거래처가 없습니다.");
		}
		return new ApiResponseVO("거래처를 수정했습니다.", id, null);
	}

	@PatchMapping(value = "/active", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseVO changeActive(@RequestBody ActiveChangeVO change) {
		ApiRequests.requireIds(change);
		if (!"Y".equals(change.getActiveFlag()) && !"N".equals(change.getActiveFlag())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "사용 여부를 확인해 주세요.");
		}
		int count = service.changeActive(change.getIds(), change.getActiveFlag());
		if (count == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "변경할 거래처가 없습니다.");
		}
		return new ApiResponseVO("거래처의 사용 여부를 변경했습니다.", null, count);
	}

	private void validate(PartnerVO partner, boolean creating) {
		if (!StringUtils.hasText(partner.getPartnerName())
				|| (creating && !StringUtils.hasText(partner.getPartnerCode()))) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "거래처 코드와 상호를 입력해 주세요.");
		}
		partner.setPartnerName(partner.getPartnerName().trim());
	}
}
