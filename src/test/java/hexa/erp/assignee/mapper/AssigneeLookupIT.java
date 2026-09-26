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
 * 실제 localhost Oracle과 main의 db.properties로 수동 실행한다(기본 mvn test/package 제외).
 * mvn -Dtest=AssigneeLookupIT test ASSIGNEE SELECT만 수행한다. DDL·seed·DML은 실행하지
 * 않으며 0건도 정상이다.
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
			// 실제 코드로 바인딩 검색도 실행한다. seed 건수나 고정 담당자명은 가정하지 않는다.
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
