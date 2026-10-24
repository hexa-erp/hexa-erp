package hexa.erp.warehouse.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.warehouse.mapper.WarehouseLookupMapper;
import hexa.erp.common.domain.LookupCriteria;

public class WarehouseLookupServiceTest {
	@Test
	public void delegatesReadMethodsWithoutChangingCriteriaOrRows() {
		final LookupCriteria expected = new LookupCriteria();
		expected.setPageNum(2);
		expected.setKeyword("00001");
		WarehouseLookupVO row = new WarehouseLookupVO();
		row.setWarehouseId(42L);
		row.setWarehouseCode("00001");
		row.setWarehouseName("テスト倉庫");
		row.setActiveFlag("Y");
		final List<WarehouseLookupVO> rows = Collections.singletonList(row);
		WarehouseLookupServiceImpl service = new WarehouseLookupServiceImpl();
		service.setMapper(new WarehouseLookupMapper() {
			@Override
			public WarehouseLookupVO read(Long id) {
				assertEquals(Long.valueOf(42L), id);
				return row;
			}

			@Override
			public List<WarehouseLookupVO> getListWithPaging(LookupCriteria criteria) {
				assertSame(expected, criteria);
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
		assertSame(rows, service.getList(expected));
		assertEquals(2, expected.getPageNum());
		assertEquals("00001", expected.getKeyword());
	}

	@Test
	public void mapperFailureIsNotReplacedWithEmptyResults() {
		final RuntimeException failure = new IllegalStateException("テスト用取得失敗");
		WarehouseLookupServiceImpl service = new WarehouseLookupServiceImpl();
		service.setMapper(new WarehouseLookupMapper() {
			@Override
			public WarehouseLookupVO read(Long id) {
				throw failure;
			}

			@Override
			public List<WarehouseLookupVO> getListWithPaging(LookupCriteria criteria) {
				throw failure;
			}

			@Override
			public int getTotalCount(LookupCriteria criteria) {
				throw failure;
			}
		});
		try {
			service.get(42L);
			fail("1件取得のエラーを隠してはならない。");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
		try {
			service.getTotal(new LookupCriteria());
			fail("件数取得エラーを隠してはならない。");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
		try {
			service.getList(new LookupCriteria());
			fail("一覧取得エラーを隠してはならない。");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
	}
}
