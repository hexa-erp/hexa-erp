package hexa.erp.assignee.service;

import java.util.List;

import hexa.erp.assignee.domain.AssigneeLookupVO;
import hexa.erp.common.domain.LookupCriteria;

public interface AssigneeLookupService {
	AssigneeLookupVO get(Long assigneeId);

	List<AssigneeLookupVO> getList(LookupCriteria criteria);

	int getTotal(LookupCriteria criteria);
}
