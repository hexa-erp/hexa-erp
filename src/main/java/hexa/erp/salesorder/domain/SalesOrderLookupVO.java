package hexa.erp.salesorder.domain;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class SalesOrderLookupVO {
	private Long documentId;
	private String documentNo;
	private String businessDate;
	private String dueDate;
	private Long partnerId;
	private String partnerCode;
	private String partnerName;
	private Long warehouseId;
	private String warehouseCode;
	private String warehouseName;
	private Long assigneeId;
	private String assigneeCode;
	private String assigneeName;
	private String note;
	private String progressStatus;
	private String itemSummary;
	private BigDecimal totalAmount;
	private BigDecimal totalQuantity;
	private List<SalesOrderLookupLineVO> lines;
}
