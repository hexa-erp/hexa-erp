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
			throw new ApiException(HttpStatus.BAD_REQUEST, "新規取引先にIDを指定することはできません。");
		}
		validate(partner, true);
		partner.setPartnerCode(partner.getPartnerCode().trim());
		try {
			service.register(partner);
		} catch (DuplicateKeyException e) {
			throw new ApiException(HttpStatus.CONFLICT, "登録済みの取引先コードです。別のコードを入力してください。");
		}
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponseVO("取引先を登録しました。", partner.getPartnerId(), null));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseVO update(@PathVariable("id") Long id, @RequestBody PartnerVO partner) {
		partner.setPartnerId(id);
		validate(partner, false);
		if (!service.modify(partner)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "更新対象の取引先がありません。");
		}
		return new ApiResponseVO("取引先を更新しました。", id, null);
	}

	@PatchMapping(value = "/active", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseVO changeActive(@RequestBody ActiveChangeVO change) {
		ApiRequests.requireIds(change);
		if (!"Y".equals(change.getActiveFlag()) && !"N".equals(change.getActiveFlag())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "利用状態を確認してください。");
		}
		int count = service.changeActive(change.getIds(), change.getActiveFlag());
		if (count == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "変更対象の取引先がありません。");
		}
		return new ApiResponseVO("取引先の利用状態を変更しました。", null, count);
	}

	private void validate(PartnerVO partner, boolean creating) {
		if (!StringUtils.hasText(partner.getPartnerName())
				|| (creating && !StringUtils.hasText(partner.getPartnerCode()))) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "取引先コードと取引先名を入力してください。");
		}
		partner.setPartnerName(partner.getPartnerName().trim());
	}
}
