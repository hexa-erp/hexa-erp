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

/** 売上集計の検索結果を月別にまとめ、月別小計と総合計を計算する。 */
@Service
@Log4j
public class SaleStatusServiceImpl implements SaleStatusService {
	@Setter(onMethod_ = @Autowired)
	private SaleStatusMapper mapper;

	@Override
	public SaleStatusVO getStatus(SaleStatusCriteria criteria) {
		log.info("売上集計の明細行を取得");
		List<SaleStatusRowVO> rows = mapper.getStatusRows(criteria);
		SaleStatusVO report = new SaleStatusVO();
		report.setTotals(makeTotals());
		String currentMonth = null;
		List<SaleStatusRowVO> monthRows = null;
		Map<String, BigDecimal> monthTotals = null;

		// 検索結果は日付順のため、データが存在する月のみグループ化する。
		for (SaleStatusRowVO row : rows) {
			String monthLabel = row.getBusinessDate().substring(0, 7);
			if (!monthLabel.equals(currentMonth)) {
				currentMonth = monthLabel;
				monthRows = new ArrayList<>();
				monthTotals = makeTotals();
				// 画面ではmonthLabel（YYYY-MM）、rows（当月の行）、totals（当月の小計）として参照する。
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

	/** 画面の表示順に合計項目を作成し、初期値を0にする。 */
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
		// 売上集計の単価は平均値ではなく、表示する明細行の単価の合計とする。
		addValue(totals, "unitPrice", row.getUnitPrice());
		addValue(totals, "supplyAmount", row.getSupplyAmount());
		addValue(totals, "vatAmount", row.getVatAmount());
		addValue(totals, "totalAmount", row.getTotalAmount());
	}

	private void addValue(Map<String, BigDecimal> totals, String key, BigDecimal value) {
		// 値がない行は集計対象から除外する。
		if (value != null) {
			totals.put(key, totals.get(key).add(value));
		}
	}

}
