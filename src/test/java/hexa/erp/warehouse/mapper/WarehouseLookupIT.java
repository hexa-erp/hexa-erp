package hexa.erp.warehouse.mapper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.service.WarehouseService;
import hexa.erp.common.domain.LookupCriteria;

/**
 * 실제 localhost Oracle과 main의 db.properties로 수동 실행한다(기본 mvn test/package 제외).
 * mvn -Dtest=WarehouseLookupIT test
 * WAREHOUSE SELECT만 수행한다. DDL·seed·DML은 실행하지 않으며 0건도 정상이다.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/spring/root-context.xml")
public class WarehouseLookupIT {
	@Inject
	private WarehouseService service;

	@Test
	public void activeWarehousesCanBeReadEvenWhenTableIsEmpty() {
		LookupCriteria criteria = new LookupCriteria();
		assertTrue(service.getTotal(criteria) >= 0);
		List<WarehouseVO> rows = service.getList(criteria);
		assertRows(rows);
		if (!rows.isEmpty()) {
			// 실제 코드로 바인딩 검색도 실행한다. seed 건수나 고정 창고명은 가정하지 않는다.
			criteria.setKeyword(rows.get(0).getWarehouseCode());
			assertTrue(service.getTotal(criteria) >= 0);
			assertRows(service.getList(criteria));
		}
		criteria.setPageNum(2);
		assertRows(service.getList(criteria));
	}

	private void assertRows(List<WarehouseVO> rows) {
		assertNotNull(rows);
		assertTrue(rows.size() <= 25);
		for (WarehouseVO row : rows) {
			assertNotNull(row.getWarehouseId());
			assertNotNull(row.getWarehouseCode());
			assertNotNull(row.getWarehouseName());
			assertEquals("Y", row.getActiveFlag());
		}
	}
}
