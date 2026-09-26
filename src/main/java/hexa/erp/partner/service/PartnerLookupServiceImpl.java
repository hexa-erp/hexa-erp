package hexa.erp.partner.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.partner.mapper.PartnerLookupMapper;
import hexa.erp.common.domain.LookupCriteria;
import lombok.Setter;

@Service
public class PartnerLookupServiceImpl implements PartnerLookupService {
	@Setter(onMethod_ = @Autowired)
	private PartnerLookupMapper mapper;

	@Override
	public PartnerLookupVO get(Long partnerId) {
		return mapper.read(partnerId);
	}

	@Override
	public List<PartnerLookupVO> getList(LookupCriteria criteria) {
		return mapper.getListWithPaging(criteria);
	}

	@Override
	public int getTotal(LookupCriteria criteria) {
		return mapper.getTotalCount(criteria);
	}
}
