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
	
	//검색 조건에 맞는 견적서 현황 조회
	@Override
	public QuotationReportVO getStatus(QuotationReportCriteria criteria) {
		log.info("견적서 현황 상세행 조회");
		
		//품목별 상세 행 조회
		List<QuotationReportRowVO> rows = mapper.getStatusRows(criteria);
		
		
		return makeReport(rows, false);

	}
	
	//검색 조건에 맞는 미주문 현황 조회
	@Override
	public QuotationReportVO getUnordered(QuotationReportCriteria criteria) {
		log.info("미주문 현황 상세 행 조회");
		
		//미주문 품목 조회 - 기준일까지의 주문 수량 차감
		List<QuotationReportRowVO> rows = mapper.getUnorderedRows(criteria);
		
		
		return makeReport(rows, true);
	}
	
	//조회 결과를 월별로 그룹화, 월별 및 전체 합계 계산
	private QuotationReportVO makeReport(List<QuotationReportRowVO> rows, boolean unordered) {
		QuotationReportVO report = new QuotationReportVO();
		report.setTotals(makeTotals(unordered));
		
		String currentMonth = null;
		List<QuotationReportRowVO> monthRows = null;
		Map<String, BigDecimal> monthTotals = null;
		
		for(QuotationReportRowVO row : rows) {
			//각 행의 거래일자에서 연월 추출
			String monthLabel = row.getBusinessDate().substring(0, 7);
			
			//새로운 월인 경우 월별 그룹 생성
			if(!monthLabel.equals(currentMonth)) {
				currentMonth = monthLabel;
				monthRows = new ArrayList<>();
				monthTotals = makeTotals(unordered);
				
				//현재 월의 행 추가 및 합계 누적
				Map<String, Object> monthGroup = new LinkedHashMap<>();
				monthGroup.put("monthLabel", monthLabel);
				monthGroup.put("rows", monthRows);
				monthGroup.put("totals",monthTotals);
				report.getMonthGroups().add(monthGroup);
				
			}
			
			monthRows.add(row);
			addTotals(monthTotals, row, unordered);
			addTotals(report.getTotals(), row, unordered);
		}
		
		return report;
	}
	
	//수량, 단가, 공급가액 합계 초기화
	private Map<String, BigDecimal> makeTotals(boolean unordered){
		Map<String, BigDecimal> totals = new LinkedHashMap<>();
		totals.put("quantity", BigDecimal.ZERO);
		
		if (unordered) {
			//미주문 현황의 잔여 수량 및 금액 합계 초기화
			totals.put("remainingQuantity", BigDecimal.ZERO);
			totals.put("remainingSupplyAmount", BigDecimal.ZERO);
			totals.put("remainingVatAmount", BigDecimal.ZERO);
		}
		else {
			//견적서 현황의 단가 및 공급가액 합계 초기화
			totals.put("unitPrice", BigDecimal.ZERO);
			totals.put("supplyAmount", BigDecimal.ZERO);
		}
		
		return totals;
		
	}
	
	//품목 벼 수량, 금액 합계에 누적
	private void addTotals(Map<String,BigDecimal> totals, QuotationReportRowVO row, boolean unordered) {
		addValue(totals, "quantity", row.getQuantity());
		
		if (unordered) {
			//미주문 현황의 잔여 수량 및 금액  누적
			addValue(totals, "remainingQuantity", row.getRemainingQuantity());
			addValue(totals, "remainingSupplyAmount", row.getRemainingSupplyAmount());
			addValue(totals, "remainingVatAmount", row.getRemainingVatAmount());
		} else {
			//견적서 현황의 단가 및 공급가액 누적
			addValue(totals, "unitPrice", row.getUnitPrice());
			addValue(totals, "supplyAmount", row.getSupplyAmount());
		}
	}
	
	//기존 합계에 값 더하기
	private void addValue(Map<String, BigDecimal> totals, String key, BigDecimal value) {
		if(value != null) {
			totals.put(key, totals.get(key).add(value));
		}
	}

	
	
}
