package hexa.erp.salesorder.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

//주문서 현황 조회 월별 결과
@Data
public class SalesOrderReportVO {

	private List<Map<String, Object>> monthGroups = new ArrayList<>();
	
	//모든 달을 더한 총 합계
	private Map<String, BigDecimal> totals = new LinkedHashMap<>();

}
