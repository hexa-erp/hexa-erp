package hexa.erp.partner.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.partner.domain.PartnerVO;
import hexa.erp.partner.mapper.PartnerMapper;
import hexa.erp.common.domain.LookupCriteria;

@Service
public class PartnerServiceImpl implements PartnerService {
	private PartnerMapper mapper;

	@Autowired
	public void setMapper(PartnerMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public List<PartnerVO> getList(LookupCriteria criteria) {
		return mapper.getList(criteria);
	}

	@Override
	public int getTotal(LookupCriteria criteria) {
		return mapper.getTotal(criteria);
	}
}
