package hexa.erp.assignee.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.assignee.domain.AssigneeLookupVO;
import hexa.erp.assignee.mapper.AssigneeLookupMapper;
import hexa.erp.common.domain.LookupCriteria;
import lombok.Setter;

@Service
public class AssigneeLookupServiceImpl implements AssigneeLookupService {
	@Setter(onMethod_ = @Autowired)
	private AssigneeLookupMapper mapper;

	@Override
	public AssigneeLookupVO get(Long assigneeId) {
		return mapper.read(assigneeId);
	}

	@Override
	public List<AssigneeLookupVO> getList(LookupCriteria criteria) {
		return mapper.getListWithPaging(criteria);
	}

	@Override
	public int getTotal(LookupCriteria criteria) {
		return mapper.getTotalCount(criteria);
	}
}
