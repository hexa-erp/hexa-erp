package hexa.erp.salesorder.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.salesorder.domain.SalesOrderReportCriteria;
import hexa.erp.salesorder.domain.SalesOrderReportRowVO;
import hexa.erp.salesorder.domain.SalesOrderReportVO;
import hexa.erp.salesorder.mapper.SalesOrderReportMapper;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
public class SalesOrderReportServiceImpl implements SalesOrderReportService {

	@Setter(onMethod_ = @Autowired)
	private SalesOrderReportMapper mapper;

	// 検索条件に一致する受注集計を取得する。
	@Override
	public SalesOrderReportVO getStatus(SalesOrderReportCriteria criteria) {
		log.info("受注集計の明細行を取得");

		// 品目別の明細行を取得する。
		List<SalesOrderReportRowVO> rows = mapper.getStatusRows(criteria);

		return makeReport(rows, false);
	}

	// 検索条件に一致する未売上一覧を取得する。
	@Override
	public SalesOrderReportVO getUnsold(SalesOrderReportCriteria criteria) {
		log.info("未売上一覧の明細行を取得");

		// 未売上品目を取得する。基準日までの売上数量を差し引く。
		List<SalesOrderReportRowVO> rows = mapper.getUnsoldRows(criteria);

		return makeReport(rows, true);
	}

	// 検索結果を月別にグループ化し、月別合計と総合計を計算する。
	private SalesOrderReportVO makeReport(List<SalesOrderReportRowVO> rows, boolean unsold) {
		SalesOrderReportVO report = new SalesOrderReportVO();
		report.setTotals(makeTotals(unsold));

		String currentMonth = null;
		List<SalesOrderReportRowVO> monthRows = null;
		Map<String, BigDecimal> monthTotals = null;

		for (SalesOrderReportRowVO row : rows) {
			// 各行の取引日から年月を抽出する。
			String monthLabel = row.getBusinessDate().substring(0, 7);

			// 新しい月の場合は月別グループを作成する。
			if (!monthLabel.equals(currentMonth)) {
				currentMonth = monthLabel;
				monthRows = new ArrayList<>();
				monthTotals = makeTotals(unsold);

				// 現在の月に行を追加し、合計に加算する。
				Map<String, Object> monthGroup = new LinkedHashMap<>();
				monthGroup.put("monthLabel", monthLabel);
				monthGroup.put("rows", monthRows);
				monthGroup.put("totals", monthTotals);
				report.getMonthGroups().add(monthGroup);

			}

			monthRows.add(row);
			addTotals(monthTotals, row, unsold);
			addTotals(report.getTotals(), row, unsold);
		}

		return report;
	}

	// 数量・単価・税抜金額の合計を初期化する。
	private Map<String, BigDecimal> makeTotals(boolean unsold) {
		Map<String, BigDecimal> totals = new LinkedHashMap<>();
		totals.put("quantity", BigDecimal.ZERO);

		if (unsold) {
			// 未売上一覧の残数量と残金額の合計を初期化する。
			totals.put("remainingQuantity", BigDecimal.ZERO);
			totals.put("remainingSupplyAmount", BigDecimal.ZERO);
			totals.put("remainingVatAmount", BigDecimal.ZERO);
		} else {
			// 受注集計の単価と税抜金額の合計を初期化する。
			totals.put("unitPrice", BigDecimal.ZERO);
			totals.put("supplyAmount", BigDecimal.ZERO);
		}

		return totals;

	}

	// 品目別の数量・金額合計に加算する。
	private void addTotals(Map<String, BigDecimal> totals, SalesOrderReportRowVO row, boolean unsold) {
		addValue(totals, "quantity", row.getQuantity());

		if (unsold) {
			// 未売上一覧の残数量と残金額を加算する。
			addValue(totals, "remainingQuantity", row.getRemainingQuantity());
			addValue(totals, "remainingSupplyAmount", row.getRemainingSupplyAmount());
			addValue(totals, "remainingVatAmount", row.getRemainingVatAmount());
		} else {
			// 受注集計の単価と税抜金額を加算する。
			addValue(totals, "unitPrice", row.getUnitPrice());
			addValue(totals, "supplyAmount", row.getSupplyAmount());
		}
	}

	// 現在の合計に値を加算する。
	private void addValue(Map<String, BigDecimal> totals, String key, BigDecimal value) {
		if (value != null) {
			totals.put(key, totals.get(key).add(value));
		}
	}

}
