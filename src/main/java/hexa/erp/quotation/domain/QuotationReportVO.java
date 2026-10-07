package hexa.erp.quotation.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class QuotationReportVO {

	private List<Map<String, Object>> monthGroups = new ArrayList<>();
	private Map<String, BigDecimal> totals = new LinkedHashMap<>();

}
