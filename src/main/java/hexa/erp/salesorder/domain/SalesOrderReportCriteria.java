package hexa.erp.salesorder.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

//주문서 현황 및 미판매 현황 검색 조건
@Data
public class SalesOrderReportCriteria {

	private String startDate;
	private String endDate;
	private String dueStartDate;
	private String dueEndDate;
	//미판매 현황: 판매 여부 및 잔여 수량 계산 기준일
	private String cutoffDate;
	
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
