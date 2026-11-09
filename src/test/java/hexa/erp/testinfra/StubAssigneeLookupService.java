package hexa.erp.testinfra;

import java.util.Collections;
import java.util.List;

import hexa.erp.assignee.domain.AssigneeLookupVO;
import hexa.erp.assignee.service.AssigneeLookupService;
import hexa.erp.common.domain.LookupCriteria;

/** Webコンテキストテスト専用レスポンス。実際のDBデータや検索処理ではなく、mainでは使用しない。 */
public class StubAssigneeLookupService implements AssigneeLookupService {
	@Override
	public AssigneeLookupVO get(Long id) {
		AssigneeLookupVO row = getList(new LookupCriteria()).get(0);
		row.setAssigneeId(id);
		row.setAssigneeName("Webテスト担当者 " + id);
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
		row.setAssigneeName("Webテスト担当者");
		row.setActiveFlag("Y");
		return Collections.singletonList(row);
	}
}
