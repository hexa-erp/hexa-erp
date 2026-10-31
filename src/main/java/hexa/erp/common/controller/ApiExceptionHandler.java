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

/** JSPを返すControllerのエラー処理と、APIのJSONエラー処理を分ける。 */
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

	// 数量・単価などが数値へ変換できない場合は、保存処理へ進まず入力形式の案内をJSONで返す。
	@ExceptionHandler({ HttpMessageNotReadableException.class, BindException.class,
			MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ApiResponseVO> handleBinding(Exception e) {
		return error(HttpStatus.BAD_REQUEST, "入力形式を確認してください。数量と単価は数値で入力してください。");
	}

	// マスターのコード重複は、別のコードを入力するよう案内する。
	@ExceptionHandler(DuplicateKeyException.class)
	public ResponseEntity<ApiResponseVO> handleDuplicate(DuplicateKeyException e) {
		return error(HttpStatus.CONFLICT, "登録済みのコードです。別のコードを入力してください。");
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiResponseVO> handleUploadSize(MaxUploadSizeExceededException e) {
		return error(HttpStatus.PAYLOAD_TOO_LARGE, "5MB以下の画像ファイルを選択してください。");
	}

	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<ApiResponseVO> handleMultipart(MultipartException e) {
		return error(HttpStatus.BAD_REQUEST, "画像アップロードのリクエストを確認してください。ファイルサイズは5MB以下にしてください。");
	}

	// DB処理に失敗した場合は、保存・状態変更・削除を呼び出した画面へエラーを返す。
	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiResponseVO> handleDatabase(DataAccessException e) {
		log.error("APIデータの処理に失敗しました", e);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "リクエストを処理できませんでした。入力内容を確認してから再度お試しください。");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponseVO> handleUnexpected(Exception e) {
		log.error("APIリクエストの処理に失敗しました", e);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "リクエストを処理できませんでした。しばらくしてから再度お試しください。");
	}

	private ResponseEntity<ApiResponseVO> error(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(new ApiResponseVO(message, null, null));
	}
}
