package hexa.erp.assignee.mapper;

import java.util.List;

import hexa.erp.assignee.domain.AssigneeLookupVO;
import hexa.erp.common.domain.LookupCriteria;

public interface AssigneeLookupMapper {
	AssigneeLookupVO read(Long assigneeId);

	List<AssigneeLookupVO> getListWithPaging(LookupCriteria criteria);

	int getTotalCount(LookupCriteria criteria);
}
