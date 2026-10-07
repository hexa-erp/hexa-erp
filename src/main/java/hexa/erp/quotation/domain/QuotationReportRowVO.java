package hexa.erp.quotation.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class QuotationReportRowVO {

	private Long quotationId;
	private Long quotationLineId;
	private String quotationNo;
	private String businessDate;
	
	private Long partnerId;
	private String partnerName;
	
	private Long warehouseId;
	private String warehouseName;
	
	private Long assigneeId;
	private String assigneeName;
	
	private String progressStatus;
	private String headerNote;
	
	private Long itemId;
	private String itemCode;
	private String itemName;
	
	private String specification;
	private String unit;
	private String note;
	
	private BigDecimal quantity;
	private BigDecimal unitPrice;
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount;
	
	
}
