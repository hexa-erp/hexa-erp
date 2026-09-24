package hexa.erp.partner.domain;

import lombok.Data;

@Data
public class PartnerVO {
	private Long partnerId;
	private String partnerCode;
	private String partnerName;
	private Long assigneeId;
	private String assigneeCode;
	private String assigneeName;
	private String activeFlag;
}
