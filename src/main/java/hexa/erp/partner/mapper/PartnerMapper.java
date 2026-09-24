package hexa.erp.partner.mapper;

import java.util.List;

import hexa.erp.partner.domain.PartnerVO;
import hexa.erp.common.domain.LookupCriteria;

public interface PartnerMapper {
	List<PartnerVO> getList(LookupCriteria criteria);

	int getTotal(LookupCriteria criteria);
}
