package hexa.erp.sale.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SaleLineVO {

	// 판매 상세 식별 정보
	private Long saleLineId;
	private Long saleId;
	private Long salesOrderLineId;

	// 품목 정보
	private Long itemId;
	private String itemCode;
	private String itemName;
	private String specification;
	private String unit;

	// 수량과 금액 정보
	private BigDecimal quantity;
	private BigDecimal unitPrice;
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount;

	// 비고와 삭제 여부
	private String note;
	private String deletedYn;
}
