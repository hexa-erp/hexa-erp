package hexa.erp.testinfra;

import java.util.Collections;
import java.util.List;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.salesorder.domain.SalesOrderLookupVO;
import hexa.erp.salesorder.service.SalesOrderLookupService;

public class StubSalesOrderLookupService implements SalesOrderLookupService {

	@Override
	public List<SalesOrderLookupVO> getList(LookupCriteria criteria, String progressStatus) {
		return Collections.emptyList();
	}

	@Override
	public int getTotal(LookupCriteria criteria, String progressStatus) {
		return 0;
	}
}
