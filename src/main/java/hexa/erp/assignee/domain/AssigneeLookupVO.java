package hexa.erp.assignee.domain;

import lombok.Data;

@Data
public class AssigneeLookupVO {
	private Long assigneeId;
	private String assigneeCode;
	private String assigneeName;
	private String activeFlag;
}
