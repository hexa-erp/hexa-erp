package hexa.erp.shipinstruction.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ShipInstructionLookupLineVO {
	private Long lineId;
	private Long itemId;
	private String itemCode;
	private String itemName;
	private String specification;
	private String unit;
	private BigDecimal quantity;
	private BigDecimal remainingQuantity;
	private String note;
}
