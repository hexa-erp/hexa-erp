package hexa.erp.item.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import hexa.erp.item.domain.ItemLookupVO;
import hexa.erp.item.mapper.ItemLookupMapper;
import hexa.erp.common.domain.LookupCriteria;

public class ItemLookupServiceTest {
	@Test
	public void delegatesReadsWithoutChangingCriteriaWarehouseOrRows() {
		final LookupCriteria expected = new LookupCriteria();
		expected.setPageNum(2);
		expected.setKeyword("00001");
		ItemLookupVO row = new ItemLookupVO();
		row.setItemId(42L);
		row.setItemCode("00001");
		row.setItemName("테스트 품목");
		row.setOutboundPrice(new BigDecimal("9999999999999999.99"));
		row.setStockQuantity(new BigDecimal("-1.005"));
		row.setActiveFlag("Y");
		final List<ItemLookupVO> rows = Collections.singletonList(row);
		for (final Long warehouseId : Arrays.asList(null, 201L, 202L, 9007199254740993L)) {
			ItemLookupServiceImpl service = new ItemLookupServiceImpl();
			service.setMapper(new ItemLookupMapper() {
				@Override
				public ItemLookupVO read(Long id) {
					assertEquals(Long.valueOf(42L), id);
					return row;
				}

				@Override
				public List<ItemLookupVO> getListWithPaging(LookupCriteria criteria, Long selectedWarehouse) {
					assertSame(expected, criteria);
					assertEquals(warehouseId, selectedWarehouse);
					return rows;
				}

				@Override
				public int getTotalCount(LookupCriteria criteria) {
					assertSame(expected, criteria);
					return 26;
				}
			});
			assertSame(row, service.get(42L));
			assertEquals(26, service.getTotal(expected));
			assertSame(rows, service.getList(expected, warehouseId));
			assertEquals(2, expected.getPageNum());
			assertEquals("00001", expected.getKeyword());
		}
	}

	@Test
	public void mapperFailureIsNotReplacedWithEmptyResults() {
		final RuntimeException failure = new IllegalStateException("테스트용 조회 실패");
		ItemLookupServiceImpl service = new ItemLookupServiceImpl();
		service.setMapper(new ItemLookupMapper() {
			@Override
			public ItemLookupVO read(Long id) {
				throw failure;
			}

			@Override
			public List<ItemLookupVO> getListWithPaging(LookupCriteria criteria, Long warehouseId) {
				throw failure;
			}

			@Override
			public int getTotalCount(LookupCriteria criteria) {
				throw failure;
			}
		});
		try {
			service.get(42L);
			fail("단건 조회 오류를 숨기면 안 된다.");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
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
