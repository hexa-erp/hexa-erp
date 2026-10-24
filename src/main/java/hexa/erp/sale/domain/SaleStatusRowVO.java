package hexa.erp.sale.domain;

import java.math.BigDecimal;

import lombok.Data;

/** 売上集計の1行。売上伝票の品目行1件に対応する。 */
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
	private String headerNote; // 伝票の備考
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
	private String note; // 品目行の備考

}
