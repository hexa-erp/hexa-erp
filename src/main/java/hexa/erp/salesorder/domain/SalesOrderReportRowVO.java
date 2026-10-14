package hexa.erp.salesorder.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SalesOrderReportRowVO {

	private Long salesOrderId;
	private Long salesOrderLineId;
	private String salesOrderNo;
	
	private String businessDate;
	private String dueDate;
	
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
