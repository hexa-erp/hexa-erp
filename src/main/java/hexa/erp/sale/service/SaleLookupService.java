package hexa.erp.sale.service;

import java.util.List;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.sale.domain.SaleLookupVO;

public interface SaleLookupService {
	List<SaleLookupVO> getList(LookupCriteria criteria, String progressStatus);

	int getTotal(LookupCriteria criteria, String progressStatus);
}
