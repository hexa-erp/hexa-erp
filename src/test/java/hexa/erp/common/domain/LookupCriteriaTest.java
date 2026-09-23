package hexa.erp.common.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LookupCriteriaTest {
	@Test
	public void defaultsMatchLookupContract() {
		LookupCriteria criteria = new LookupCriteria();
		assertEquals(1, criteria.getPageNum());
		assertEquals(25, criteria.getAmount());
		assertEquals("", criteria.getKeyword());
	}

	@Test
	public void pageNeverFallsBelowOne() {
		LookupCriteria criteria = new LookupCriteria();
		for (int page : new int[] { Integer.MIN_VALUE, -1, 0, 1 }) {
			criteria.setPageNum(page);
			assertEquals(1, criteria.getPageNum());
		}
		criteria.setPageNum(Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, criteria.getPageNum());
	}

	@Test
	public void keywordIsTrimmedWithoutChangingCode() {
		LookupCriteria criteria = new LookupCriteria();
		criteria.setKeyword("  00001  ");
		assertEquals("00001", criteria.getKeyword());
		criteria.setKeyword("  김 담당자  ");
		assertEquals("김 담당자", criteria.getKeyword());
		criteria.setKeyword(null);
		assertEquals("", criteria.getKeyword());
		criteria.setKeyword("   ");
		assertEquals("", criteria.getKeyword());
	}
}
