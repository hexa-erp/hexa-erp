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

	// 選択した売上明細行の単価と金額を既存Serviceでまとめて更新する。
	@PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> changePrices(@RequestBody SalePriceFormVO form) {
		int count = service.savePrices(form.getChanges());
		return ResponseEntity.ok(new ApiResponseVO("売上単価を変更しました。", null, count));
	}
}
