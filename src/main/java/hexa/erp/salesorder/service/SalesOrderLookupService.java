package hexa.erp.salesorder.service;

import java.util.List;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.salesorder.domain.SalesOrderLookupVO;

public interface SalesOrderLookupService {
	List<SalesOrderLookupVO> getList(LookupCriteria criteria, String progressStatus);

	int getTotal(LookupCriteria criteria, String progressStatus);
}
