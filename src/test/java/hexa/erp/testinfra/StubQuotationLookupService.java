package hexa.erp.testinfra;

import java.util.Collections;
import java.util.List;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.quotation.domain.QuotationLookupVO;
import hexa.erp.quotation.service.QuotationLookupService;

public class StubQuotationLookupService implements QuotationLookupService {

	@Override
	public List<QuotationLookupVO> getList(LookupCriteria criteria, String progressStatus) {
		return Collections.emptyList();
	}

	@Override
	public int getTotal(LookupCriteria criteria, String progressStatus) {
		return 0;
	}
}
