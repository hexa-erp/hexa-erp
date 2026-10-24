package hexa.erp.quotation.controller;

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
import hexa.erp.quotation.domain.QuotationVO;
import hexa.erp.quotation.service.QuotationService;
import lombok.Setter;

@RestController
@RequestMapping(value = "/api/quotations", produces = MediaType.APPLICATION_JSON_VALUE)
public class QuotationApiController {

	@Setter(onMethod_ = @Autowired)
	private QuotationService service;

	// 見積書1件と品目明細を取得する。
	@GetMapping("/{id}")
	public QuotationVO get(@PathVariable("id") Long id) {
		QuotationVO document = service.get(id);
		if (document == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, "表示対象の見積書がありません。");
		}
		return document;
	}

	// 新規見積書を登録する。
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> create(@RequestBody QuotationVO form) {
		if (form.getQuotationId() != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "新規登録時に伝票IDを指定することはできません。");
		}
		Long id = service.save(form);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).body(new ApiResponseVO("見積書を保存しました。", id, null));
	}

	// パスのIDに対応する見積書を更新する。
	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> update(@PathVariable("id") Long id, @RequestBody QuotationVO form) {
		if (service.get(id) == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, "更新対象の見積書がありません。");
		}
		form.setQuotationId(id);
		service.save(form);
		return ResponseEntity.ok(new ApiResponseVO("見積書を更新しました。", id, null));
	}

	// 選択した見積書を削除する。
	@DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> delete(@RequestBody SelectedIdsVO form) {
		int count = service.remove(ApiRequests.requireIds(form));
		if (count == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "削除対象の見積書がありません。");
		}
		return ResponseEntity.ok(new ApiResponseVO("見積書を削除しました。", null, count));
	}

	// 選択した見積書の進行状態を変更する。
	@PatchMapping(value = "/status", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> changeStatus(@RequestBody StatusChangeVO form) {
		List<Long> ids = ApiRequests.requireIds(form);
		int count = service.changeStatus(ids, form.getProgressStatus());
		if (count == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "進行状態を変更する見積書がありません。");
		}
		return ResponseEntity.ok(new ApiResponseVO("進行状態を変更しました。", null, count));
	}
}
