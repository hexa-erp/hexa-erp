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

	// 검색조건에 맞는 주문서 현황 조회
	@Override
	public SalesOrderReportVO getStatus(SalesOrderReportCriteria criteria) {
		log.info("주문서 현황 상세행 조회");

		// 품목별 상세 행 조회
		List<SalesOrderReportRowVO> rows = mapper.getStatusRows(criteria);

		return makeReport(rows, false);
	}

	// 검색조건에 맞는 미판매 현황 조회
	@Override
	public SalesOrderReportVO getUnsold(SalesOrderReportCriteria criteria) {
		log.info("미판매 현황 상세행 조회");

		// 미판매 품목 조회 - 기준일까지의 판매 수량 차감
		List<SalesOrderReportRowVO> rows = mapper.getUnsoldRows(criteria);

		return makeReport(rows, true);
	}

	// 조회 결과를 월별로 그룹화, 월별 및 전체 합계 계산
	private SalesOrderReportVO makeReport(List<SalesOrderReportRowVO> rows, boolean unsold) {
		SalesOrderReportVO report = new SalesOrderReportVO();
		report.setTotals(makeTotals(unsold));

		String currentMonth = null;
		List<SalesOrderReportRowVO> monthRows = null;
		Map<String, BigDecimal> monthTotals = null;

		for (SalesOrderReportRowVO row : rows) {
			// 각 행의 거래일자에서 연월 추출
			String monthLabel = row.getBusinessDate().substring(0, 7);

			// 새로운 월인 경우 월별 그룹 생성
			if (!monthLabel.equals(currentMonth)) {
				currentMonth = monthLabel;
				monthRows = new ArrayList<>();
				monthTotals = makeTotals(unsold);

				// 현재 월의 행 추가 및 합계 누적
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

	// 수량, 단가, 공급가액 합계 초기화
	private Map<String, BigDecimal> makeTotals(boolean unsold) {
		Map<String, BigDecimal> totals = new LinkedHashMap<>();
		totals.put("quantity", BigDecimal.ZERO);

		if (unsold) {
			// 미판매 현황의 잔여 수량 및 금액 합계 초기화
			totals.put("remainingQuantity", BigDecimal.ZERO);
			totals.put("remainingSupplyAmount", BigDecimal.ZERO);
			totals.put("remainingVatAmount", BigDecimal.ZERO);
		} else {
			// 주문서 현황의 단가 및 공급가액 합계 초기화
			totals.put("unitPrice", BigDecimal.ZERO);
			totals.put("supplyAmount", BigDecimal.ZERO);
		}

		return totals;

	}

	// 품목 별 수량, 금액 합계에 누적
	private void addTotals(Map<String, BigDecimal> totals, SalesOrderReportRowVO row, boolean unsold) {
		addValue(totals, "quantity", row.getQuantity());

		if (unsold) {
			// 미판매 현황의 잔여 수량 및 금액 누적
			addValue(totals, "remainingQuantity", row.getRemainingQuantity());
			addValue(totals, "remainingSupplyAmount", row.getRemainingSupplyAmount());
			addValue(totals, "remainingVatAmount", row.getRemainingVatAmount());
		} else {
			// 주문서 현황의 단가 및 공급가액 누적
			addValue(totals, "unitPrice", row.getUnitPrice());
			addValue(totals, "supplyAmount", row.getSupplyAmount());
		}
	}

	// 기존 합계에 값 더하기
	private void addValue(Map<String, BigDecimal> totals, String key, BigDecimal value) {
		if (value != null) {
			totals.put(key, totals.get(key).add(value));
		}
	}

}
