package hexa.erp.quotation.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

//見積集計の月別結果
@Data
public class QuotationReportVO {

	private List<Map<String, Object>> monthGroups = new ArrayList<>();

	// 全月の総合計
	private Map<String, BigDecimal> totals = new LinkedHashMap<>();

}
