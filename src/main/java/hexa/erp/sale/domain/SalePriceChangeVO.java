package hexa.erp.sale.domain;

import java.math.BigDecimal;

import lombok.Data;

/** 単価を変更する売上品目1件。画面の1行に対応する入力値。 */
@Data
public class SalePriceChangeVO {
	// チェックボックスの選択状態。未選択の場合は値が送信されずfalseになる。
	private boolean selected;
	private Long saleLineId;
	private Long saleId;
	// 変更後の単価
	private BigDecimal unitPrice;

}
