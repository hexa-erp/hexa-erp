package hexa.erp.salesorder.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

//受注集計の月別結果
@Data
public class SalesOrderReportVO {

	private List<Map<String, Object>> monthGroups = new ArrayList<>();

	// 全月の総合計
	private Map<String, BigDecimal> totals = new LinkedHashMap<>();

}
