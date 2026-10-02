package hexa.erp.sale.domain;

import java.math.BigDecimal;

import lombok.Data;

/** 판매 현황의 한 행. 판매 전표의 품목 행 하나가 한 행이 된다. */
@Data
public class SaleStatusRowVO {
	private Long saleId;
	private Long saleLineId;
	private String saleNo;
	private String businessDate; // YYYY-MM-DD
	private Long partnerId;
	private String partnerName;
	private Long warehouseId;
	private String warehouseName;
	private Long assigneeId;
	private String assigneeName;
	private String progressStatus;
	private String headerNote; // 전표 비고
	private Long itemId;
	private String itemCode;
	private String itemName;
	private String specification;
	private String unit;
	private BigDecimal quantity;
	private BigDecimal unitPrice;
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount;
	private String note; // 품목 행 비고

}
