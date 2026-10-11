package hexa.erp.quotation.service;

import hexa.erp.quotation.domain.QuotationReportCriteria;
import hexa.erp.quotation.domain.QuotationReportVO;

public interface QuotationReportService {

	//견적서 현황 조회 및 월별 합계 
	QuotationReportVO getStatus(QuotationReportCriteria criteria);
}
