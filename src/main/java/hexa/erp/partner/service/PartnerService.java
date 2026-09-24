package hexa.erp.partner.service;

import java.util.List;

import hexa.erp.partner.domain.PartnerVO;
import hexa.erp.common.domain.LookupCriteria;

public interface PartnerService {
	List<PartnerVO> getList(LookupCriteria criteria);

	int getTotal(LookupCriteria criteria);
}
