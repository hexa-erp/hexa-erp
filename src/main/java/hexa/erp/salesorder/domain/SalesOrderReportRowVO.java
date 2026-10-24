package hexa.erp.salesorder.domain;

import java.math.BigDecimal;

import lombok.Data;

//受注集計と未売上一覧の品目行別明細
@Data
public class SalesOrderReportRowVO {

	private Long salesOrderId;
	private Long salesOrderLineId;
	private String salesOrderNo;

	private String businessDate;
	private String dueDate;

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

	// 受注伝票に登録されている数量
	private BigDecimal quantity;

	private BigDecimal unitPrice;
	private BigDecimal supplyAmount;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount;

	// 未売上一覧：売上数量を差し引いた残数量
	private BigDecimal remainingQuantity;

	// 未売上一覧：残数量を基にした税抜金額と消費税
	private BigDecimal remainingSupplyAmount;
	private BigDecimal remainingVatAmount;

}
