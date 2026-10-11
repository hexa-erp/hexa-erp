package hexa.erp.sale.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.sale.domain.SaleStatusCriteria;
import hexa.erp.sale.domain.SaleStatusRowVO;
import hexa.erp.sale.domain.SaleStatusVO;
import hexa.erp.sale.mapper.SaleStatusMapper;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

/** 판매 현황 조회 결과를 월별로 묶고, 월별 소계와 전체 합계를 계산한다. */
@Service
@Log4j
public class SaleStatusServiceImpl implements SaleStatusService {
	@Setter(onMethod_ = @Autowired)
	private SaleStatusMapper mapper;

	@Override
	public SaleStatusVO getStatus(SaleStatusCriteria criteria) {
		log.info("판매 현황 상세행 조회");
		List<SaleStatusRowVO> rows = mapper.getStatusRows(criteria);
		SaleStatusVO report = new SaleStatusVO();
		report.setTotals(makeTotals());
		String currentMonth = null;
		List<SaleStatusRowVO> monthRows = null;
		Map<String, BigDecimal> monthTotals = null;

		// 조회 결과가 날짜순이므로 행이 있는 월만 묶는다.
		for (SaleStatusRowVO row : rows) {
			String monthLabel = row.getBusinessDate().substring(0, 7);
			if (!monthLabel.equals(currentMonth)) {
				currentMonth = monthLabel;
				monthRows = new ArrayList<>();
				monthTotals = makeTotals();
				// 화면에서 monthLabel(YYYY-MM), rows(그 달의 행), totals(그 달의 소계)로 꺼내 쓴다.
				Map<String, Object> monthGroup = new LinkedHashMap<>();
				monthGroup.put("monthLabel", monthLabel);
				monthGroup.put("rows", monthRows);
				monthGroup.put("totals", monthTotals);
				report.getMonthGroups().add(monthGroup);
			}
			monthRows.add(row);
			addTotals(monthTotals, row);
			addTotals(report.getTotals(), row);
		}
		return report;
	}

	/** 합계 항목을 화면 표시 순서대로 0으로 채워 만든다. */
	private Map<String, BigDecimal> makeTotals() {
		Map<String, BigDecimal> totals = new LinkedHashMap<>();
		totals.put("quantity", BigDecimal.ZERO);
		totals.put("unitPrice", BigDecimal.ZERO);
		totals.put("supplyAmount", BigDecimal.ZERO);
		totals.put("vatAmount", BigDecimal.ZERO);
		totals.put("totalAmount", BigDecimal.ZERO);
		return totals;
	}

	private void addTotals(Map<String, BigDecimal> totals, SaleStatusRowVO row) {
		addValue(totals, "quantity", row.getQuantity());
		// 판매 현황의 단가는 평균이 아니라 표시한 상세행 단가의 합계다.
		addValue(totals, "unitPrice", row.getUnitPrice());
		addValue(totals, "supplyAmount", row.getSupplyAmount());
		addValue(totals, "vatAmount", row.getVatAmount());
		addValue(totals, "totalAmount", row.getTotalAmount());
	}

	private void addValue(Map<String, BigDecimal> totals, String key, BigDecimal value) {
		// 값이 비어 있는 행은 합계에서 제외한다.
		if (value != null) {
			totals.put(key, totals.get(key).add(value));
		}
	}

}
