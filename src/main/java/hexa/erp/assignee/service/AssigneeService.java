package hexa.erp.assignee.service;

import java.util.List;

import hexa.erp.assignee.domain.AssigneeVO;
import hexa.erp.common.domain.LookupCriteria;

public interface AssigneeService {
	List<AssigneeVO> getList(LookupCriteria criteria);

	int getTotal(LookupCriteria criteria);
}
