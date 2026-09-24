package hexa.erp.warehouse.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.mapper.WarehouseMapper;
import hexa.erp.common.domain.LookupCriteria;

public class WarehouseServiceTest {
	@Test
	public void delegatesBothReadMethodsWithoutChangingCriteriaOrRows() {
		final LookupCriteria expected = new LookupCriteria();
		expected.setPageNum(2);
		expected.setKeyword("00001");
		WarehouseVO row = new WarehouseVO();
		row.setWarehouseId(42L);
		row.setWarehouseCode("00001");
		row.setWarehouseName("테스트 창고");
		row.setActiveFlag("Y");
		final List<WarehouseVO> rows = Collections.singletonList(row);
		WarehouseServiceImpl service = new WarehouseServiceImpl();
		service.setMapper(new WarehouseMapper() {
			@Override
			public List<WarehouseVO> getList(LookupCriteria criteria) {
				assertSame(expected, criteria);
				return rows;
			}

			@Override
			public int getTotal(LookupCriteria criteria) {
				assertSame(expected, criteria);
				return 26;
			}
		});
		assertEquals(26, service.getTotal(expected));
		assertSame(rows, service.getList(expected));
		assertEquals(2, expected.getPageNum());
		assertEquals("00001", expected.getKeyword());
	}

	@Test
	public void mapperFailureIsNotReplacedWithEmptyResults() {
		final RuntimeException failure = new IllegalStateException("테스트용 조회 실패");
		WarehouseServiceImpl service = new WarehouseServiceImpl();
		service.setMapper(new WarehouseMapper() {
			@Override
			public List<WarehouseVO> getList(LookupCriteria criteria) {
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
			service.getList(new LookupCriteria());
			fail("목록 오류를 숨기면 안 된다.");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
	}
}
