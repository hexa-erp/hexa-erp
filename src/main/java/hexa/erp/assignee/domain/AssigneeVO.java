package hexa.erp.assignee.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Data;

@Data
public class AssigneeVO {
	// NUMBER(18) ID가 브라우저에서 반올림되지 않도록 JSON은 문자열로 보낸다.
	@JsonSerialize(using = ToStringSerializer.class)
	private Long assigneeId;
	private String assigneeCode;
	private String assigneeName;
	private String activeFlag;
}
