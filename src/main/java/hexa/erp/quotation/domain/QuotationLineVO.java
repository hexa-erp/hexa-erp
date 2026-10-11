package hexa.erp.quotation.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class QuotationLineVO {
	
	private Long quotationLineId;
	private Long quotationId;
	
	private Long itemId;
	private String itemCode;
	private String itemName;
	
	private String specification;
	private String unit;
	
	private BigDecimal quantity;
	private BigDecimal unitPrice;
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private String note;
	private BigDecimal totalAmount;
	
	private String deletedYn;
	
}
