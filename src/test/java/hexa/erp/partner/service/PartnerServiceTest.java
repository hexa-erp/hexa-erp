package hexa.erp.partner.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import hexa.erp.partner.domain.PartnerVO;
import hexa.erp.partner.mapper.PartnerMapper;
import hexa.erp.common.domain.LookupCriteria;

public class PartnerServiceTest {
	@Test
	public void delegatesBothReadMethodsWithoutChangingCriteriaOrRows() {
		final LookupCriteria expected = new LookupCriteria();
		expected.setPageNum(2);
		expected.setKeyword("00001");
		PartnerVO row = new PartnerVO();
		row.setPartnerId(42L);
		row.setPartnerCode("00001");
		row.setPartnerName("테스트 거래처");
		row.setActiveFlag("Y");
		final List<PartnerVO> rows = Collections.singletonList(row);
		PartnerServiceImpl service = new PartnerServiceImpl();
		service.setMapper(new PartnerMapper() {
			@Override
			public List<PartnerVO> getList(LookupCriteria criteria) {
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
		PartnerServiceImpl service = new PartnerServiceImpl();
		service.setMapper(new PartnerMapper() {
			@Override
			public List<PartnerVO> getList(LookupCriteria criteria) {
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
