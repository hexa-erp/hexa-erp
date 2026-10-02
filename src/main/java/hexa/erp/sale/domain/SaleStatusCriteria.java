package hexa.erp.sale.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/** 판매 현황 검색 조건. 값이 비어 있는 조건은 검색에 적용하지 않는다. */
@Data
public class SaleStatusCriteria {
	// 날짜는 YYYY-MM-DD 형식이며, 종료일 당일까지 포함한다.
	private String startDate;
	private String endDate;
	private String keyword;
	private String progressStatus;
	private String specification;
	// 수량·금액 범위 조건. min은 이상, max는 이하로 비교한다.
	private BigDecimal minQuantity;
	private BigDecimal maxQuantity;
	private BigDecimal minUnitPrice;
	private BigDecimal maxUnitPrice;
	private BigDecimal minSupplyAmount;
	private BigDecimal maxSupplyAmount;
	private BigDecimal minVatAmount;
	private BigDecimal maxVatAmount;
	// 필터 선택창에서 고른 ID 목록. 같은 목록 안에서는 OR로 검색한다.
	private List<String> warehouseIds = new ArrayList<>();
	private List<String> partnerIds = new ArrayList<>();
	private List<String> itemIds = new ArrayList<>();
	private List<String> assigneeIds = new ArrayList<>();

}
