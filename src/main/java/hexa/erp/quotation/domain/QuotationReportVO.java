package hexa.erp.quotation.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

//견적서 현황 조회 월별 결과
@Data
public class QuotationReportVO {

	private List<Map<String, Object>> monthGroups = new ArrayList<>();
	
	//모든 달을 더한 총 합계
	private Map<String, BigDecimal> totals = new LinkedHashMap<>();

}
