package hexa.erp.item.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import hexa.erp.item.domain.ItemVO;
import hexa.erp.item.mapper.ItemMapper;
import hexa.erp.common.domain.LookupCriteria;

public class ItemServiceTest {
	@Test
	public void delegatesReadsWithoutChangingCriteriaWarehouseOrRows() {
		final LookupCriteria expected = new LookupCriteria();
		expected.setPageNum(2);
		expected.setKeyword("00001");
		ItemVO row = new ItemVO();
		row.setItemId(42L);
		row.setItemCode("00001");
		row.setItemName("테스트 품목");
		row.setOutboundPrice(new BigDecimal("9999999999999999.99"));
		row.setStockQuantity(new BigDecimal("-1.005"));
		row.setActiveFlag("Y");
		final List<ItemVO> rows = Collections.singletonList(row);
		for (final Long warehouseId : Arrays.asList(null, 201L, 202L, 9007199254740993L)) {
			ItemServiceImpl service = new ItemServiceImpl();
			service.setMapper(new ItemMapper() {
				@Override
				public List<ItemVO> getList(LookupCriteria criteria, Long selectedWarehouse) {
					assertSame(expected, criteria);
					assertEquals(warehouseId, selectedWarehouse);
					return rows;
				}

				@Override
				public int getTotal(LookupCriteria criteria) {
					assertSame(expected, criteria);
					return 26;
				}
			});
			assertEquals(26, service.getTotal(expected));
			assertSame(rows, service.getList(expected, warehouseId));
			assertEquals(2, expected.getPageNum());
			assertEquals("00001", expected.getKeyword());
		}
	}

	@Test
	public void mapperFailureIsNotReplacedWithEmptyResults() {
		final RuntimeException failure = new IllegalStateException("테스트용 조회 실패");
		ItemServiceImpl service = new ItemServiceImpl();
		service.setMapper(new ItemMapper() {
			@Override
			public List<ItemVO> getList(LookupCriteria criteria, Long warehouseId) {
				throw failure;
			}

			@Override
			public int getTotal(LookupCriteria criteria) {
				throw failure;
			}
		});
		try {
			service.getTotal(new LookupCriteria());
			fail("건수 오류를 숨기면 안 된다.");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
		try {
			service.getList(new LookupCriteria(), 201L);
			fail("목록 오류를 숨기면 안 된다.");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
	}
}
