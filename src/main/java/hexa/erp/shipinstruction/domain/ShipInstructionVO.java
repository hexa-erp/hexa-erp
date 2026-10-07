package hexa.erp.shipinstruction.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ShipInstructionVO {

	private Long shipInstructionId;
	private String shipInstructionNo;
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
	private String plannedDate;
	private String deliveryContact;
	private String deliveryPostalCode;
	private String deliveryAddress;
	private String progressStatus = "IN_PROGRESS";
	private String updatedAt;
	private String deletedYn;
	private List<ShipInstructionLineVO> lines = new ArrayList<>();
	private List<Long> removedLineIds = new ArrayList<>();
}
