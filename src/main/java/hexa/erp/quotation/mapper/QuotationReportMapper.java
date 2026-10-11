package hexa.erp.quotation.mapper;

import java.util.List;

import hexa.erp.quotation.domain.QuotationReportCriteria;
import hexa.erp.quotation.domain.QuotationReportRowVO;

public interface QuotationReportMapper {

	//검색조건에 따른 견적서 현황 품목 별 조회
	List<QuotationReportRowVO> getStatusRows(QuotationReportCriteria criteria);
}
