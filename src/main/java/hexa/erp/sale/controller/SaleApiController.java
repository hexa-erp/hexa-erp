package hexa.erp.sale.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import hexa.erp.common.controller.ApiException;
import hexa.erp.common.controller.ApiRequests;
import hexa.erp.common.domain.ApiResponseVO;
import hexa.erp.common.domain.SelectedIdsVO;
import hexa.erp.common.domain.StatusChangeVO;
import hexa.erp.sale.domain.SaleVO;
import hexa.erp.sale.service.SaleService;
import lombok.Setter;

@RestController
@RequestMapping(value = "/api/sales", produces = MediaType.APPLICATION_JSON_VALUE)
public class SaleApiController {

	@Setter(onMethod_ = @Autowired)
	private SaleService service;

	// 売上情報1件と品目明細を取得する。
	@GetMapping("/{id}")
	public SaleVO get(@PathVariable("id") Long id) {
		SaleVO document = service.get(id);
		if (document == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, "表示対象の売上情報がありません。");
		}
		return document;
	}

	// 新規売上情報を登録する。
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> create(@RequestBody SaleVO form) {
		if (form.getSaleId() != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "新規登録時に伝票IDを指定することはできません。");
		}
		Long id = service.save(form);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).body(new ApiResponseVO("売上情報を保存しました。", id, null));
	}

	// パスのIDに対応する売上情報を更新する。
	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> update(@PathVariable("id") Long id, @RequestBody SaleVO form) {
		if (service.get(id) == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, "更新対象の売上情報がありません。");
		}
		form.setSaleId(id);
		service.save(form);
		return ResponseEntity.ok(new ApiResponseVO("売上情報を更新しました。", id, null));
	}

	// 選択した売上情報を削除する。
	@DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> delete(@RequestBody SelectedIdsVO form) {
		int count = service.remove(ApiRequests.requireIds(form));
		if (count == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "削除対象の売上情報がありません。");
		}
		return ResponseEntity.ok(new ApiResponseVO("売上情報を削除しました。", null, count));
	}

	// 選択した売上情報の進行状態を変更する。
	@PatchMapping(value = "/status", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> changeStatus(@RequestBody StatusChangeVO form) {
		List<Long> ids = ApiRequests.requireIds(form);
		int count = service.changeStatus(ids, form.getProgressStatus());
		if (count == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "進行状態を変更する売上情報がありません。");
		}
		return ResponseEntity.ok(new ApiResponseVO("進行状態を変更しました。", null, count));
	}
}
