package hexa.erp.sale.domain;

import java.math.BigDecimal;

import lombok.Data;

/** 단가를 변경할 판매 품목 한 건. 화면의 행 하나에서 넘어온 값이다. */
@Data
public class SalePriceChangeVO {
	// 체크박스 선택 여부. 체크하지 않으면 값이 넘어오지 않아 false가 된다.
	private boolean selected;
	private Long saleLineId;
	private Long saleId;
	// 새로 적용할 단가
	private BigDecimal unitPrice;

}
