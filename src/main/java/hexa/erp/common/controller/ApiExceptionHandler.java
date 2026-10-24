package hexa.erp.common.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import hexa.erp.common.domain.ApiResponseVO;
import lombok.extern.log4j.Log4j;

/** JSP 반환 Controller의 오류 처리와 API의 JSON 오류 처리를 구분한다. */
@Log4j
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiResponseVO> handleApi(ApiException e) {
		return error(e.getStatus(), e.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponseVO> handleValidation(IllegalArgumentException e) {
		return error(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, BindException.class,
			MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ApiResponseVO> handleBinding(Exception e) {
		return error(HttpStatus.BAD_REQUEST, "입력값의 형식을 확인해 주세요. 수량과 단가는 숫자로 입력해 주세요.");
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public ResponseEntity<ApiResponseVO> handleDuplicate(DuplicateKeyException e) {
		return error(HttpStatus.CONFLICT, "이미 등록된 코드입니다. 다른 코드를 입력해 주세요.");
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiResponseVO> handleUploadSize(MaxUploadSizeExceededException e) {
		return error(HttpStatus.PAYLOAD_TOO_LARGE, "이미지 파일은 5MB 이하로 선택해 주세요.");
	}

	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<ApiResponseVO> handleMultipart(MultipartException e) {
		return error(HttpStatus.BAD_REQUEST, "이미지 업로드 요청을 확인해 주세요. 파일 크기는 5MB 이하여야 합니다.");
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiResponseVO> handleDatabase(DataAccessException e) {
		log.error("API 데이터 처리 실패", e);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "요청을 처리하지 못했습니다. 입력 내용을 확인한 뒤 다시 시도해 주세요.");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponseVO> handleUnexpected(Exception e) {
		log.error("API 요청 처리 실패", e);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.");
	}

	private ResponseEntity<ApiResponseVO> error(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(new ApiResponseVO(message, null, null));
	}
}
