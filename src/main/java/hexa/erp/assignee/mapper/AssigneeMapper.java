package hexa.erp.assignee.mapper;

import java.util.List;

import hexa.erp.assignee.domain.AssigneeVO;
import hexa.erp.common.domain.LookupCriteria;

public interface AssigneeMapper {
	List<AssigneeVO> getList(LookupCriteria criteria);

	int getTotal(LookupCriteria criteria);
}
