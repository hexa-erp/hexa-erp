package hexa.erp.partner.domain;

import lombok.Data;

@Data
public class PartnerVO {

    private Long partnerId;
    private String partnerCode;
    private String partnerName;
    private String businessNo;
    private String representative;
    private String businessType;
    private String businessItem;
    private String phone;
    private String mobile;
    private String email;
    private String postalCode;
    private String address;
    private Long assigneeId;
    private String assigneeName;
    private String note;
    private String activeFlag;
    private String updatedAt;
}
