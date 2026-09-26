package hexa.erp.partner.mapper;

import java.util.List;

import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.common.domain.LookupCriteria;

public interface PartnerLookupMapper {
	PartnerLookupVO read(Long partnerId);

	List<PartnerLookupVO> getListWithPaging(LookupCriteria criteria);

	int getTotalCount(LookupCriteria criteria);
}
