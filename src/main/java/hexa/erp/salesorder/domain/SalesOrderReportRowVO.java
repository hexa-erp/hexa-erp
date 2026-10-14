package hexa.erp.salesorder.domain;

import java.math.BigDecimal;

import lombok.Data;

//주문서 현황 및 미판매 현황 품목 행 별 상세
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
	
	//주문서에 등록된 기존 수량
	private BigDecimal quantity;
	
	private BigDecimal unitPrice;
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount;

	//미판매 현황: 판매 수량을 차감한 잔여 수량
	private BigDecimal remainingQuantity;
	
	//미판매 현황: 잔여 수량 기준 공급가액 및 부가세
	private BigDecimal remainingSupplyAmount;
	private BigDecimal remainingVatAmount;
	

}
