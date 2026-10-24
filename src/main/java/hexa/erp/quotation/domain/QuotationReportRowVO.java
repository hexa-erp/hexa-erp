package hexa.erp.quotation.domain;

import java.math.BigDecimal;

import lombok.Data;

//見積集計と未受注一覧の品目行別明細
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

	// 見積書に登録されている数量
	private BigDecimal quantity;

	private BigDecimal unitPrice;
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount;

	// 未受注一覧：受注数量を差し引いた残数量
	private BigDecimal remainingQuantity;

	// 未受注一覧：残数量を基にした税抜金額と消費税
	private BigDecimal remainingSupplyAmount;
	private BigDecimal remainingVatAmount;

}
