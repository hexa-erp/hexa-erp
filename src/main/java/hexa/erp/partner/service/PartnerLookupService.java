package hexa.erp.partner.service;

import java.util.List;

import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.common.domain.LookupCriteria;

public interface PartnerLookupService {
	PartnerLookupVO get(Long partnerId);

	List<PartnerLookupVO> getList(LookupCriteria criteria);

	int getTotal(LookupCriteria criteria);
}
