package hexa.erp.sale.domain;

import java.math.BigDecimal;

import lombok.Data;

/** 売上単価一括変更の一覧行。売上品目の1明細行に対応する。 */
@Data
public class SalePriceVO {
	// 売上明細行IDと、その明細が属する売上伝票ID
	private Long saleLineId;
	private Long saleId;
	private String businessDate;
	private String saleNo;
	private String partnerName;
	private String assigneeName;
	private String warehouseName;
	// 売上の元となる伝票（受注など）の日付と番号
	private String sourceBusinessDate;
	private String sourceDocumentNo;
	private String itemCode;
	private String itemName;
	private String specification;
	private BigDecimal quantity;
	private String unit;
	private BigDecimal unitPrice;
	// 税抜金額 + 消費税 = 合計金額
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount;
	private String note;
	private String createdAt;
	private String updatedAt;

}
