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
		
		//조회 결과 월결 그룹화 및 합계 계산
		return makeReport(rows);

	}
	
	//조회 결과를 월별로 그룹화, 월별 및 전체 합계 계산
	private QuotationReportVO makeReport(List<QuotationReportRowVO> rows) {
		QuotationReportVO report = new QuotationReportVO();
		report.setTotals(makeTotals());
		
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
				monthTotals = makeTotals();
				
				//현재 월의 행 추가 및 합계 누적
				Map<String, Object> monthGroup = new LinkedHashMap<>();
				monthGroup.put("monthLabel", monthLabel);
				monthGroup.put("rows", monthRows);
				monthGroup.put("totals",monthTotals);
				report.getMonthGroups().add(monthGroup);
				
			}
			
			monthRows.add(row);
			addTotals(monthTotals, row);
			addTotals(report.getTotals(), row);
		}
		
		return report;
	}
	
	//수량, 단가, 공급가액 합계 초기화
	private Map<String, BigDecimal> makeTotals(){
		Map<String, BigDecimal> totals = new LinkedHashMap<>();
		totals.put("quantity", BigDecimal.ZERO);
		totals.put("unitPrice", BigDecimal.ZERO);
		totals.put("supplyAmount", BigDecimal.ZERO);
		
		return totals;
		
	}
	
	//행의 수량, 단가, 공급가액 합계에 누적
	private void addTotals(Map<String,BigDecimal> totals, QuotationReportRowVO row) {
		addValue(totals, "quantity", row.getQuantity());
		addValue(totals, "unitPrice", row.getUnitPrice());
		addValue(totals, "supplyAmount", row.getSupplyAmount());
		
	}
	
	//기존 합계에 값 더하기
	private void addValue(Map<String, BigDecimal> totals, String key, BigDecimal value) {
		if(value != null) {
			totals.put(key, totals.get(key).add(value));
		}
	}
}
