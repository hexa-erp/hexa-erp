package hexa.erp.assignee.mapper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import hexa.erp.assignee.domain.AssigneeLookupVO;
import hexa.erp.assignee.service.AssigneeLookupService;
import hexa.erp.common.domain.LookupCriteria;

/**
 * 実際のlocalhostのOracleとmainのdb.propertiesを使用して手動実行する（通常のmvn test/packageでは除外）。
 * mvn -Dtest=AssigneeLookupIT test。ASSIGNEEへのSELECTのみ実行する。
 * DDL・seed・DMLは実行せず、0件でも正常とする。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/spring/root-context.xml")
public class AssigneeLookupIT {
	@Inject
	private AssigneeLookupService service;

	@Test
	public void activeAssigneesCanBeReadEvenWhenTableIsEmpty() {
		LookupCriteria criteria = new LookupCriteria();
		assertTrue(service.getTotal(criteria) >= 0);
		List<AssigneeLookupVO> rows = service.getList(criteria);
		assertRows(rows);
		if (!rows.isEmpty()) {
			// 実際のコードでバインド検索も実行する。seed件数や固定の担当者名は前提にしない。
			criteria.setKeyword(rows.get(0).getAssigneeCode());
			assertTrue(service.getTotal(criteria) >= 0);
			assertRows(service.getList(criteria));
		}
		criteria.setPageNum(2);
		assertRows(service.getList(criteria));
	}

	private void assertRows(List<AssigneeLookupVO> rows) {
		assertNotNull(rows);
		assertTrue(rows.size() <= 25);
		for (AssigneeLookupVO row : rows) {
			assertNotNull(row.getAssigneeId());
			assertNotNull(row.getAssigneeCode());
			assertNotNull(row.getAssigneeName());
			assertEquals("Y", row.getActiveFlag());
		}
	}
}
