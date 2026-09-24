package hexa.erp.partner.mapper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import hexa.erp.partner.domain.PartnerVO;
import hexa.erp.partner.service.PartnerService;
import hexa.erp.common.domain.LookupCriteria;

/**
 * 실제 localhost Oracle과 main의 db.properties로 수동 실행한다(기본 mvn test/package 제외).
 * mvn -Dtest=PartnerLookupIT test
 * BIZ_PARTNER·ASSIGNEE SELECT만 수행한다. DDL·seed·DML은 실행하지 않으며 0건도 정상이다.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/spring/root-context.xml")
public class PartnerLookupIT {
	@Inject
	private PartnerService service;

	@Inject
	private DataSource dataSource;

	@Test
	public void activePartnersCanBeReadEvenWhenTableIsEmpty() {
		LookupCriteria criteria = new LookupCriteria();
		assertTrue(service.getTotal(criteria) >= 0);
		List<PartnerVO> rows = service.getList(criteria);
		assertRows(rows);
		if (!rows.isEmpty()) {
			// 실제 코드로 바인딩 검색도 실행한다. seed 건수나 고정 거래처명은 가정하지 않는다.
			criteria.setKeyword(rows.get(0).getPartnerCode());
			assertTrue(service.getTotal(criteria) >= 0);
			assertRows(service.getList(criteria));
		}
		criteria.setPageNum(2);
		assertRows(service.getList(criteria));
	}

	@Test
	public void joinedAssigneeMatchesMasterWithoutLosingUnassignedPartners() {
		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		LookupCriteria criteria = new LookupCriteria();
		int total = jdbc.queryForObject("SELECT COUNT(*) FROM BIZ_PARTNER WHERE ACTIVE_FLAG = 'Y'", Integer.class);
		assertEquals(total, service.getTotal(criteria));
		int seen = 0;
		for (int page = 1; page <= Math.max(1, (int) Math.ceil(total / 25.0)); page++) {
			criteria.setPageNum(page);
			List<PartnerVO> rows = service.getList(criteria);
			seen += rows.size();
			for (PartnerVO row : rows) {
				Long assigneeId = jdbc.queryForObject(
						"SELECT ASSIGNEE_ID FROM BIZ_PARTNER WHERE PARTNER_ID = ?", Long.class, row.getPartnerId());
				assertEquals(assigneeId, row.getAssigneeId());
				if (assigneeId == null) {
					assertNull(row.getAssigneeCode());
					assertNull(row.getAssigneeName());
				} else {
					Map<String, Object> assignee = jdbc.queryForMap(
							"SELECT ASSIGNEE_CODE, ASSIGNEE_NAME FROM ASSIGNEE WHERE ASSIGNEE_ID = ?", assigneeId);
					assertEquals(assignee.get("ASSIGNEE_CODE"), row.getAssigneeCode());
					assertEquals(assignee.get("ASSIGNEE_NAME"), row.getAssigneeName());
				}
			}
		}
		assertEquals(total, seen);
	}

	private void assertRows(List<PartnerVO> rows) {
		assertNotNull(rows);
		assertTrue(rows.size() <= 25);
		for (PartnerVO row : rows) {
			assertNotNull(row.getPartnerId());
			assertNotNull(row.getPartnerCode());
			assertNotNull(row.getPartnerName());
			assertEquals("Y", row.getActiveFlag());
		}
	}
}
