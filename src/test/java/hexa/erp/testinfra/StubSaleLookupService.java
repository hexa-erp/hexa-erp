package hexa.erp.testinfra;

import java.util.Collections;
import java.util.List;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.sale.domain.SaleLookupVO;
import hexa.erp.sale.service.SaleLookupService;

public class StubSaleLookupService implements SaleLookupService {

	@Override
	public List<SaleLookupVO> getList(LookupCriteria criteria, String progressStatus) {
		return Collections.emptyList();
	}

	@Override
	public int getTotal(LookupCriteria criteria, String progressStatus) {
		return 0;
	}
}
