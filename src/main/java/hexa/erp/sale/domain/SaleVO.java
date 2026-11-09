package hexa.erp.sale.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SaleVO {

	// 売上の識別情報
	private Long saleId;
	private String saleNo;
	private String businessDate;

	// 取引先情報
	private Long partnerId;
	private String partnerCode;
	private String partnerName;

	// 出荷倉庫の情報
	private Long warehouseId;
	private String warehouseCode;
	private String warehouseName;

	// 担当者情報
	private Long assigneeId;
	private String assigneeCode;
	private String assigneeName;

	// 売上の状態情報
	private String note;
	private String progressStatus = "CONFIRMED";
	private String createdAt;
	private String updatedAt;
	private String deletedYn;

	// 売上一覧の表示情報
	private String itemSummary;
	private BigDecimal totalAmount;

	// 売上明細情報
	private List<SaleLineVO> lines = new ArrayList<>();

	private List<Long> removedLineIds = new ArrayList<>();
}
