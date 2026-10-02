package hexa.erp.quotation.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class QuotationVO {

	private Long quotationId;
	private String quotationNo;
	
	private String businessDate;
	
	private Long partnerId;
	private String partnerCode;
	private String partnerName;
	
	private Long assigneeId;
	private String assigneeCode;
	private String assigneeName;
	
	private Long warehouseId;
	private String warehouseCode;
	private String warehouseName;
	
	private String note;
	private String progressStatus = "IN_PROGRESS";
	
	private String updatedAt;
	private String deletedYn;
	
	private String itemSummary;
	private BigDecimal totalAmount;
	    
	private List<QuotationLineVO> lines = new ArrayList<>();
	private List<Long> removedLineIds = new ArrayList<>();
	
}
