package hexa.erp.item.mapper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import hexa.erp.item.domain.ItemLookupVO;
import hexa.erp.item.service.ItemLookupService;
import hexa.erp.common.domain.LookupCriteria;

/**
 * 실제 localhost Oracle과 main의 db.properties로 수동 실행한다(기본 mvn test/package 제외).
 * mvn -Dtest=ItemLookupIT test ITEM·WAREHOUSE·STOCK SELECT만 수행한다. DDL·seed·DML은
 * 실행하지 않으며 0건도 정상이다.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/spring/root-context.xml")
public class ItemLookupIT {
	@Inject
	private ItemLookupService service;

	@Inject
	private DataSource dataSource;

	@Test
	public void activeItemsCanBeReadEvenWhenTableIsEmpty() {
		LookupCriteria criteria = new LookupCriteria();
		assertTrue(service.getTotal(criteria) >= 0);
		List<ItemLookupVO> rows = service.getList(criteria, null);
		assertRows(rows, null);
		if (!rows.isEmpty()) {
			// 실제 코드로 바인딩 검색도 실행한다. seed 건수나 고정 품목명은 가정하지 않는다.
			criteria.setKeyword(rows.get(0).getItemCode());
			assertTrue(service.getTotal(criteria) >= 0);
			assertRows(service.getList(criteria, null), null);
		}
		criteria.setPageNum(2);
		assertRows(service.getList(criteria, null), null);
	}

	@Test
	public void selectedWarehouseChangesOnlyStockNotTheItemList() {
		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		List<Long> warehouseIds = jdbc.queryForList("SELECT WAREHOUSE_ID FROM WAREHOUSE WHERE ROWNUM <= 2", Long.class);
		// 존재하지 않는 창고도 조회만 한다. 재고 없는 품목의 0 표시·목록 보존을 확인한다.
		Long absentWarehouse = jdbc.queryForObject("SELECT NVL(MAX(WAREHOUSE_ID), 0) + 1 FROM WAREHOUSE", Long.class);
		warehouseIds.add(absentWarehouse);
		LookupCriteria criteria = new LookupCriteria();
		for (int page : new int[] { 1, 2 }) {
			criteria.setPageNum(page);
			List<ItemLookupVO> withoutWarehouse = service.getList(criteria, null);
			int total = service.getTotal(criteria);
			for (Long warehouseId : warehouseIds) {
				List<ItemLookupVO> rows = service.getList(criteria, warehouseId);
				assertRows(rows, warehouseId);
				assertEquals(withoutWarehouse.size(), rows.size());
				assertEquals(total, service.getTotal(criteria));
				for (int index = 0; index < rows.size(); index++) {
					ItemLookupVO row = rows.get(index);
					assertEquals(withoutWarehouse.get(index).getItemId(), row.getItemId());
					List<BigDecimal> quantities = jdbc.queryForList(
							"SELECT QUANTITY FROM STOCK WHERE WAREHOUSE_ID = ? AND ITEM_ID = ?", BigDecimal.class,
							warehouseId, row.getItemId());
					assertTrue(quantities.size() <= 1);
					BigDecimal expected = quantities.isEmpty() ? BigDecimal.ZERO : quantities.get(0);
					assertEquals(0, expected.compareTo(row.getStockQuantity()));
					if (warehouseId.equals(absentWarehouse)) {
						assertTrue(quantities.isEmpty());
					}
				}
			}
		}
	}

	private void assertRows(List<ItemLookupVO> rows, Long warehouseId) {
		assertNotNull(rows);
		assertTrue(rows.size() <= 25);
		for (ItemLookupVO row : rows) {
			assertNotNull(row.getItemId());
			assertNotNull(row.getItemCode());
			assertNotNull(row.getItemName());
			assertNotNull(row.getOutboundPrice());
			if (warehouseId == null) {
				assertNull(row.getStockQuantity());
			} else {
				assertNotNull(row.getStockQuantity());
			}
			assertEquals("Y", row.getActiveFlag());
		}
	}
}
