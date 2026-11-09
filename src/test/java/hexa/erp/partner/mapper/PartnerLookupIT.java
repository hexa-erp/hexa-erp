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

import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.partner.service.PartnerLookupService;
import hexa.erp.common.domain.LookupCriteria;

/**
 * 実際のlocalhostのOracleとmainのdb.propertiesを使用して手動実行する（通常のmvn test/packageでは除外）。
 * mvn -Dtest=PartnerLookupIT test。BIZ_PARTNER・ASSIGNEEへのSELECTのみ実行する。
 * DDL・seed・DMLは実行せず、0件でも正常とする。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/spring/root-context.xml")
public class PartnerLookupIT {
	@Inject
	private PartnerLookupService service;

	@Inject
	private DataSource dataSource;

	@Test
	public void activePartnersCanBeReadEvenWhenTableIsEmpty() {
		LookupCriteria criteria = new LookupCriteria();
		assertTrue(service.getTotal(criteria) >= 0);
		List<PartnerLookupVO> rows = service.getList(criteria);
		assertRows(rows);
		if (!rows.isEmpty()) {
			// 実際のコードでバインド検索も実行する。seed件数や固定の取引先名は前提にしない。
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
			List<PartnerLookupVO> rows = service.getList(criteria);
			seen += rows.size();
			for (PartnerLookupVO row : rows) {
				Long assigneeId = jdbc.queryForObject("SELECT ASSIGNEE_ID FROM BIZ_PARTNER WHERE PARTNER_ID = ?",
						Long.class, row.getPartnerId());
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

	private void assertRows(List<PartnerLookupVO> rows) {
		assertNotNull(rows);
		assertTrue(rows.size() <= 25);
		for (PartnerLookupVO row : rows) {
			assertNotNull(row.getPartnerId());
			assertNotNull(row.getPartnerCode());
			assertNotNull(row.getPartnerName());
			assertEquals("Y", row.getActiveFlag());
		}
	}
}
