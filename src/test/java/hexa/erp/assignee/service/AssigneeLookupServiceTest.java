package hexa.erp.assignee.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import hexa.erp.assignee.domain.AssigneeLookupVO;
import hexa.erp.assignee.mapper.AssigneeLookupMapper;
import hexa.erp.common.domain.LookupCriteria;

public class AssigneeLookupServiceTest {
	@Test
	public void delegatesReadMethodsWithoutChangingCriteriaOrRows() {
		final LookupCriteria expected = new LookupCriteria();
		expected.setPageNum(2);
		expected.setKeyword("00001");
		AssigneeLookupVO row = new AssigneeLookupVO();
		row.setAssigneeId(42L);
		row.setAssigneeCode("00001");
		row.setAssigneeName("테스트 담당자");
		row.setActiveFlag("Y");
		final List<AssigneeLookupVO> rows = Collections.singletonList(row);
		AssigneeLookupServiceImpl service = new AssigneeLookupServiceImpl();
		service.setMapper(new AssigneeLookupMapper() {
			@Override
			public AssigneeLookupVO read(Long id) {
				assertEquals(Long.valueOf(42L), id);
				return row;
			}

			@Override
			public List<AssigneeLookupVO> getListWithPaging(LookupCriteria criteria) {
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
		final RuntimeException failure = new IllegalStateException("테스트용 조회 실패");
		AssigneeLookupServiceImpl service = new AssigneeLookupServiceImpl();
		service.setMapper(new AssigneeLookupMapper() {
			@Override
			public AssigneeLookupVO read(Long id) {
				throw failure;
			}

			@Override
			public List<AssigneeLookupVO> getListWithPaging(LookupCriteria criteria) {
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
			service.getList(new LookupCriteria());
			fail("목록 오류를 숨기면 안 된다.");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
	}
}
