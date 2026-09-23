package hexa.erp.assignee.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.assignee.domain.AssigneeVO;
import hexa.erp.assignee.mapper.AssigneeMapper;
import hexa.erp.common.domain.LookupCriteria;

@Service
public class AssigneeServiceImpl implements AssigneeService {
	private AssigneeMapper mapper;

	@Autowired
	public void setMapper(AssigneeMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public List<AssigneeVO> getList(LookupCriteria criteria) {
		return mapper.getList(criteria);
	}

	@Override
	public int getTotal(LookupCriteria criteria) {
		return mapper.getTotal(criteria);
	}
}
