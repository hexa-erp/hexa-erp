package hexa.erp.shipment.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ShipmentVO {
	private Long shipmentId;
	private String shipmentNo;
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
	private String deliveryContact;
	private String deliveryPostalCode;
	private String deliveryAddress;
	private String progressStatus = "CONFIRMED";
	private String updatedAt;
	private String deletedYn;
	private String itemSummary;
	private BigDecimal totalQuantity;
	private List<ShipmentLineVO> lines = new ArrayList<>();
	private List<Long> removedLineIds = new ArrayList<>();

}
