package hexa.erp.testinfra;

import java.util.Collections;
import java.util.List;

import hexa.erp.assignee.domain.AssigneeLookupVO;
import hexa.erp.assignee.service.AssigneeLookupService;
import hexa.erp.common.domain.LookupCriteria;

/** 웹 컨텍스트 테스트 전용 응답. 실제 DB 데이터나 검색 구현이 아니며 main에서는 사용하지 않는다. */
public class StubAssigneeLookupService implements AssigneeLookupService {
	@Override
	public AssigneeLookupVO get(Long id) {
		AssigneeLookupVO row = getList(new LookupCriteria()).get(0);
		row.setAssigneeId(id);
		row.setAssigneeName("웹 테스트 담당자 " + id);
		return row;
	}

	@Override
	public int getTotal(LookupCriteria criteria) {
		return 1;
	}

	@Override
	public List<AssigneeLookupVO> getList(LookupCriteria criteria) {
		AssigneeLookupVO row = new AssigneeLookupVO();
		row.setAssigneeId(42L);
		row.setAssigneeCode("00001");
		row.setAssigneeName("웹 테스트 담당자");
		row.setActiveFlag("Y");
		return Collections.singletonList(row);
	}
}
