package hexa.erp.partner.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.partner.mapper.PartnerLookupMapper;
import hexa.erp.common.domain.LookupCriteria;

public class PartnerLookupServiceTest {
	@Test
	public void delegatesReadMethodsWithoutChangingCriteriaOrRows() {
		final LookupCriteria expected = new LookupCriteria();
		expected.setPageNum(2);
		expected.setKeyword("00001");
		PartnerLookupVO row = new PartnerLookupVO();
		row.setPartnerId(42L);
		row.setPartnerCode("00001");
		row.setPartnerName("テスト取引先");
		row.setActiveFlag("Y");
		final List<PartnerLookupVO> rows = Collections.singletonList(row);
		PartnerLookupServiceImpl service = new PartnerLookupServiceImpl();
		service.setMapper(new PartnerLookupMapper() {
			@Override
			public PartnerLookupVO read(Long id) {
				assertEquals(Long.valueOf(42L), id);
				return row;
			}

			@Override
			public List<PartnerLookupVO> getListWithPaging(LookupCriteria criteria) {
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
		PartnerLookupServiceImpl service = new PartnerLookupServiceImpl();
		service.setMapper(new PartnerLookupMapper() {
			@Override
			public PartnerLookupVO read(Long id) {
				throw failure;
			}

			@Override
			public List<PartnerLookupVO> getListWithPaging(LookupCriteria criteria) {
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
