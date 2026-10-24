package hexa.erp.common.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** API 처리 결과. 오류도 같은 message 필드로 전달한다. */
@Getter
@AllArgsConstructor
public class ApiResponseVO {
	private final String message;
	private final Long id;
	private final Integer count;
}
