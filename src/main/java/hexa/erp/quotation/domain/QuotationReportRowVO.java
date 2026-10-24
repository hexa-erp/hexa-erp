package hexa.erp.quotation.domain;

import java.math.BigDecimal;

import lombok.Data;

//견적서 현황 및 미주문 현황 품목 행 별 상세 
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

	// 견적서에 등록된 기존 수량
	private BigDecimal quantity;

	private BigDecimal unitPrice;
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount;

	// 미주문 현황: 주문 수량을 차감한 잔여 수량
	private BigDecimal remainingQuantity;

	// 미주문 현황: 잔여 수량 기준 공급가액 및 부가세
	private BigDecimal remainingSupplyAmount;
	private BigDecimal remainingVatAmount;

}
