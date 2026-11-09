package hexa.erp.common.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** APIの処理結果。エラーも同じmessageフィールドで返す。 */
@Getter
@AllArgsConstructor
public class ApiResponseVO {
	private final String message;
	private final Long id;
	private final Integer count;
}
