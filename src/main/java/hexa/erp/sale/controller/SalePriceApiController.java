package hexa.erp.sale.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hexa.erp.common.domain.ApiResponseVO;
import hexa.erp.sale.domain.SalePriceFormVO;
import hexa.erp.sale.service.SalePriceService;
import lombok.Setter;

@RestController
@RequestMapping(value = "/api/sales/prices", produces = MediaType.APPLICATION_JSON_VALUE)
public class SalePriceApiController {
	@Setter(onMethod_ = @Autowired)
	private SalePriceService service;

	// 선택한 판매 상세행의 단가와 금액을 기존 서비스에서 함께 갱신한다.
	@PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> changePrices(@RequestBody SalePriceFormVO form) {
		int count = service.savePrices(form.getChanges());
		return ResponseEntity.ok(new ApiResponseVO("판매 단가를 변경했습니다.", null, count));
	}
}
