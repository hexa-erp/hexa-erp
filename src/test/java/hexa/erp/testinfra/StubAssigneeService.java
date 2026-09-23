package hexa.erp.testinfra;

import java.util.Collections;
import java.util.List;

import hexa.erp.assignee.domain.AssigneeVO;
import hexa.erp.assignee.service.AssigneeService;
import hexa.erp.common.domain.LookupCriteria;

/** 웹 컨텍스트 테스트 전용 응답. 실제 DB 데이터나 검색 구현이 아니며 main에서는 사용하지 않는다. */
public class StubAssigneeService implements AssigneeService {
	@Override
	public int getTotal(LookupCriteria criteria) {
		return 1;
	}

	@Override
	public List<AssigneeVO> getList(LookupCriteria criteria) {
		AssigneeVO row = new AssigneeVO();
		row.setAssigneeId(9007199254740993L);
		row.setAssigneeCode("00001");
		row.setAssigneeName("웹 테스트 담당자");
		row.setActiveFlag("Y");
		return Collections.singletonList(row);
	}
}
