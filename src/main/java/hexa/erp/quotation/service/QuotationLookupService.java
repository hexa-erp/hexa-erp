package hexa.erp.quotation.service;

import java.util.List;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.quotation.domain.QuotationLookupVO;

public interface QuotationLookupService {
	List<QuotationLookupVO> getList(LookupCriteria criteria, String progressStatus);

	int getTotal(LookupCriteria criteria, String progressStatus);
}
