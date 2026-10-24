package hexa.erp.salesorder.domain;

import java.math.BigDecimal;

import lombok.Data;

//주문서 품목 행 
@Data
public class SalesOrderLineVO {

	private Long salesOrderLineId;
	private Long salesOrderId;
	private Long quotationLineId;

	private Long itemId;
	private String itemCode;
	private String itemName;

	private String specification;
	private String note;

	private String unit;
	private BigDecimal quantity;
	private BigDecimal unitPrice;
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount;

	private String deletedYn;

}
