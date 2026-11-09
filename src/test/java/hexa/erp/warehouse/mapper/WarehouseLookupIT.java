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

import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.warehouse.service.WarehouseLookupService;
import hexa.erp.common.domain.LookupCriteria;

/**
 * 実際のlocalhostのOracleとmainのdb.propertiesを使用して手動実行する（通常のmvn test/packageでは除外）。
 * mvn -Dtest=WarehouseLookupIT test。WAREHOUSEへのSELECTのみ実行する。
 * DDL・seed・DMLは実行せず、0件でも正常とする。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/spring/root-context.xml")
public class WarehouseLookupIT {
	@Inject
	private WarehouseLookupService service;

	@Test
	public void activeWarehousesCanBeReadEvenWhenTableIsEmpty() {
		LookupCriteria criteria = new LookupCriteria();
		assertTrue(service.getTotal(criteria) >= 0);
		List<WarehouseLookupVO> rows = service.getList(criteria);
		assertRows(rows);
		if (!rows.isEmpty()) {
			// 実際のコードでバインド検索も実行する。seed件数や固定の倉庫名は前提にしない。
			criteria.setKeyword(rows.get(0).getWarehouseCode());
			assertTrue(service.getTotal(criteria) >= 0);
			assertRows(service.getList(criteria));
		}
		criteria.setPageNum(2);
		assertRows(service.getList(criteria));
	}

	private void assertRows(List<WarehouseLookupVO> rows) {
		assertNotNull(rows);
		assertTrue(rows.size() <= 25);
		for (WarehouseLookupVO row : rows) {
			assertNotNull(row.getWarehouseId());
			assertNotNull(row.getWarehouseCode());
			assertNotNull(row.getWarehouseName());
			assertEquals("Y", row.getActiveFlag());
		}
	}
}
