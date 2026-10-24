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

	// 견적서 한 건과 상세 품목을 조회한다.
	@GetMapping("/{id}")
	public QuotationVO get(@PathVariable("id") Long id) {
		QuotationVO document = service.get(id);
		if (document == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, "조회할 견적서 정보가 없습니다.");
		}
		return document;
	}

	// 신규 견적서를 등록한다.
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> create(@RequestBody QuotationVO form) {
		if (form.getQuotationId() != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "신규 등록에는 전표 ID를 지정할 수 없습니다.");
		}
		Long id = service.save(form);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).body(new ApiResponseVO("견적서를 저장했습니다.", id, null));
	}

	// 경로의 ID에 해당하는 견적서를 수정한다.
	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> update(@PathVariable("id") Long id, @RequestBody QuotationVO form) {
		if (service.get(id) == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, "수정할 견적서 정보가 없습니다.");
		}
		form.setQuotationId(id);
		service.save(form);
		return ResponseEntity.ok(new ApiResponseVO("견적서를 수정했습니다.", id, null));
	}

	// 선택한 견적서를 삭제한다.
	@DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> delete(@RequestBody SelectedIdsVO form) {
		int count = service.remove(ApiRequests.requireIds(form));
		if (count == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "삭제할 견적서 정보가 없습니다.");
		}
		return ResponseEntity.ok(new ApiResponseVO("견적서를 삭제했습니다.", null, count));
	}

	// 선택한 견적서의 진행상태를 변경한다.
	@PatchMapping(value = "/status", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponseVO> changeStatus(@RequestBody StatusChangeVO form) {
		List<Long> ids = ApiRequests.requireIds(form);
		int count = service.changeStatus(ids, form.getProgressStatus());
		if (count == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "진행상태를 변경할 견적서 정보가 없습니다.");
		}
		return ResponseEntity.ok(new ApiResponseVO("진행상태를 변경했습니다.", null, count));
	}
}
