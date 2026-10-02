package hexa.erp.sale.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

/** 판매 현황 화면에 넘기는 결과. */
@Data
public class SaleStatusVO {
	// 월별로 묶은 행 목록
	private List<Map<String, Object>> monthGroups = new ArrayList<>();
	// 검색 결과 전체의 수량·금액 합계 (항목 이름 → 합계)
	private Map<String, BigDecimal> totals = new LinkedHashMap<>();

}
