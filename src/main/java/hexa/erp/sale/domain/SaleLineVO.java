package hexa.erp.sale.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SaleLineVO {

	// 売上明細の識別情報
	private Long saleLineId;
	private Long saleId;
	private Long salesOrderLineId;

	// 品目情報
	private Long itemId;
	private String itemCode;
	private String itemName;
	private String specification;
	private String unit;

	// 数量と金額の情報
	private BigDecimal quantity;
	private BigDecimal unitPrice;
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount;

	// 備考と削除フラグ
	private String note;
	private String deletedYn;
}
