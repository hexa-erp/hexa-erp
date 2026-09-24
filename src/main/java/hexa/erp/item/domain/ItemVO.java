package hexa.erp.item.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ItemVO {
	private Long itemId;
	private String itemCode;
	private String itemName;
	private String specification;
	private String unit;
	private BigDecimal outboundPrice;
	private BigDecimal stockQuantity;
	private String activeFlag;
}
