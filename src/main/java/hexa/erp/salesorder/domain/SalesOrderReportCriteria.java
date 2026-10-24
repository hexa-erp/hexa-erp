package hexa.erp.salesorder.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

//受注集計と未売上一覧の検索条件
@Data
public class SalesOrderReportCriteria {

	private String startDate;
	private String endDate;
	private String dueStartDate;
	private String dueEndDate;
	// 未売上一覧：売上の有無と残数量を判定する基準日
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
