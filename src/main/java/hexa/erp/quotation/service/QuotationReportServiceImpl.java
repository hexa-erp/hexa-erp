package hexa.erp.quotation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.quotation.domain.QuotationReportCriteria;
import hexa.erp.quotation.domain.QuotationReportRowVO;
import hexa.erp.quotation.domain.QuotationReportVO;
import hexa.erp.quotation.mapper.QuotationReportMapper;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
public class QuotationReportServiceImpl implements QuotationReportService {

	@Setter(onMethod_ = @Autowired)
	private QuotationReportMapper mapper;

	// 検索条件に一致する見積集計を取得する。
	@Override
	public QuotationReportVO getStatus(QuotationReportCriteria criteria) {
		log.info("見積集計の明細行を取得");

		// 品目別の明細行を取得する。
		List<QuotationReportRowVO> rows = mapper.getStatusRows(criteria);

		return makeReport(rows, false);

	}

	// 検索条件に一致する未受注一覧を取得する。
	@Override
	public QuotationReportVO getUnordered(QuotationReportCriteria criteria) {
		log.info("未受注一覧の明細行を取得");

		// 未受注品目を取得する。基準日までの受注数量を差し引く。
		List<QuotationReportRowVO> rows = mapper.getUnorderedRows(criteria);

		return makeReport(rows, true);
	}

	// 検索結果を月別にグループ化し、月別合計と総合計を計算する。
	private QuotationReportVO makeReport(List<QuotationReportRowVO> rows, boolean unordered) {
		QuotationReportVO report = new QuotationReportVO();
		report.setTotals(makeTotals(unordered));

		String currentMonth = null;
		List<QuotationReportRowVO> monthRows = null;
		Map<String, BigDecimal> monthTotals = null;

		for (QuotationReportRowVO row : rows) {
			// 各行の取引日から年月を抽出する。
			String monthLabel = row.getBusinessDate().substring(0, 7);

			// 新しい月の場合は月別グループを作成する。
			if (!monthLabel.equals(currentMonth)) {
				currentMonth = monthLabel;
				monthRows = new ArrayList<>();
				monthTotals = makeTotals(unordered);

				// 現在の月に行を追加し、合計に加算する。
				Map<String, Object> monthGroup = new LinkedHashMap<>();
				monthGroup.put("monthLabel", monthLabel);
				monthGroup.put("rows", monthRows);
				monthGroup.put("totals", monthTotals);
				report.getMonthGroups().add(monthGroup);

			}

			monthRows.add(row);
			addTotals(monthTotals, row, unordered);
			addTotals(report.getTotals(), row, unordered);
		}

		return report;
	}

	// 数量・単価・税抜金額の合計を初期化する。
	private Map<String, BigDecimal> makeTotals(boolean unordered) {
		Map<String, BigDecimal> totals = new LinkedHashMap<>();
		totals.put("quantity", BigDecimal.ZERO);

		if (unordered) {
			// 未受注一覧の残数量と残金額の合計を初期化する。
			totals.put("remainingQuantity", BigDecimal.ZERO);
			totals.put("remainingSupplyAmount", BigDecimal.ZERO);
			totals.put("remainingVatAmount", BigDecimal.ZERO);
		} else {
			// 見積集計の単価と税抜金額の合計を初期化する。
			totals.put("unitPrice", BigDecimal.ZERO);
			totals.put("supplyAmount", BigDecimal.ZERO);
		}

		return totals;

	}

	// 品目別の数量・金額合計に加算する。
	private void addTotals(Map<String, BigDecimal> totals, QuotationReportRowVO row, boolean unordered) {
		addValue(totals, "quantity", row.getQuantity());

		if (unordered) {
			// 未受注一覧の残数量と残金額を加算する。
			addValue(totals, "remainingQuantity", row.getRemainingQuantity());
			addValue(totals, "remainingSupplyAmount", row.getRemainingSupplyAmount());
			addValue(totals, "remainingVatAmount", row.getRemainingVatAmount());
		} else {
			// 見積集計の単価と税抜金額を加算する。
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
