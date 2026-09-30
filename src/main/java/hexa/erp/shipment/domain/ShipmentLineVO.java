package hexa.erp.shipment.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ShipmentLineVO {
	private Long shipmentLineId;
	private Long shipmentId;
	private Long shipInstructionLineId;
	private Long itemId;
	private String itemCode;
	private String itemName;
	private String specification;
	private String unit;
	private BigDecimal quantity;
	private String note;
	private String deletedYn;
}
