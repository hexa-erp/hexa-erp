package hexa.erp.quotation.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class QuotationReportCriteria {
 
	private String startDate;
	private String endDate;
	private String quotationNo;
	private String keyword;
	private String progressStatus;
	private String specification;
	
	private BigDecimal minQuantity;
	private BigDecimal maxQuantity;
	private BigDecimal minUnitPrice;
	private BigDecimal maxUnitPrice;
	private BigDecimal minSupplyAmount;
	private BigDecimal maxSupplyAmount;
	private BigDecimal minVatAmount;
	private BigDecimal maxVatAmount;
	
	private List<String> warehouseIds = new ArrayList<>();
	private List<String> partnerIds = new ArrayList<>();
	private List<String> itemIds = new ArrayList<>();
	private List<String> assigneeIds = new ArrayList<>();
}
