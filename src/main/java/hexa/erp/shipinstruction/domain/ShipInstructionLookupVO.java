package hexa.erp.shipinstruction.domain;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ShipInstructionLookupVO {
	private Long documentId;
	private String documentNo;
	private String businessDate;
	private Long partnerId;
	private String partnerCode;
	private String partnerName;
	private Long warehouseId;
	private String warehouseCode;
	private String warehouseName;
	private Long assigneeId;
	private String assigneeCode;
	private String assigneeName;
	private String note;
	private String progressStatus;
	private String plannedDate;
	private String deliveryContact;
	private String deliveryPostalCode;
	private String deliveryAddress;
	private String itemSummary;
	private BigDecimal totalQuantity;
	private List<ShipInstructionLookupLineVO> lines;
}
