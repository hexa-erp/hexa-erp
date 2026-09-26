package hexa.erp.quotation.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class QuotationLookupLineVO {
	private Long lineId;
	private Long itemId;
	private String itemCode;
	private String itemName;
	private String specification;
	private String unit;
	private BigDecimal quantity;
	private BigDecimal remainingQuantity;
	private BigDecimal unitPrice;
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount;
	private String note;
}
