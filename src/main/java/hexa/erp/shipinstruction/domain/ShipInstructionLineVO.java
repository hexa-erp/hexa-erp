package hexa.erp.shipinstruction.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ShipInstructionLineVO {
	private Long shipInstructionLineId;
	private Long shipInstructionId;
	private Long itemId;
	private Long saleLineId;
	private String itemCode;
	private String itemName;
	private String specification;
	private String unit;
	private BigDecimal quantity;
	private String note;
	private String deletedYn;
	
}
