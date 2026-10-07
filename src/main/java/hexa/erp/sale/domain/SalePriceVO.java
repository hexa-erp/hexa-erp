package hexa.erp.sale.domain;

import java.math.BigDecimal;

import lombok.Data;

/** 판매 단가 일괄변경 목록의 한 행. 판매 품목(라인) 하나에 해당한다. */
@Data
public class SalePriceVO {
	// 판매 품목 ID와 그 품목이 속한 판매 전표 ID
	private Long saleLineId;
	private Long saleId;
	private String businessDate;
	private String saleNo;
	private String partnerName;
	private String assigneeName;
	private String warehouseName;
	// 판매의 근거가 된 원본 전표(주문 등)의 일자와 번호
	private String sourceBusinessDate;
	private String sourceDocumentNo;
	private String itemCode;
	private String itemName;
	private String specification;
	private BigDecimal quantity;
	private String unit;
	private BigDecimal unitPrice;
	// 공급가액 + 부가세 = 합계금액
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount;
	private String note;
	private String createdAt;
	private String updatedAt;

}
