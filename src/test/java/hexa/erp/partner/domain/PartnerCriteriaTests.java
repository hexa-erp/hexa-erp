package hexa.erp.partner.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import lombok.extern.log4j.Log4j;

@Log4j
public class PartnerCriteriaTests {

	// デフォルトの検索条件を確認
	@Test
	public void testDefaultValues() {
		PartnerCriteria criteria = new PartnerCriteria();

		log.info("========== CRITERIA DEFAULT TEST ==========");
		log.info("PAGE: " + criteria.getPage());
		log.info("PAGE SIZE: " + criteria.getPageSize());
		log.info("KEYWORD: [" + criteria.getKeyword() + "]");
		log.info("INCLUDE INACTIVE: " + criteria.getIncludeInactive());

		assertEquals(1, criteria.getPage());
		assertEquals(50, criteria.getPageSize());
		assertEquals("", criteria.getKeyword());
		assertEquals("N", criteria.getIncludeInactive());
	}

	// ページと検索語の補正を確認
	@Test
	public void testValueCorrection() {
		PartnerCriteria criteria = new PartnerCriteria();

		criteria.setPage(0);
		criteria.setPageSize(0);
		criteria.setKeyword("  テスト検索語  ");

		log.info("========== CRITERIA CORRECTION TEST ==========");
		log.info("CORRECTED PAGE: " + criteria.getPage());
		log.info("CORRECTED PAGE SIZE: " + criteria.getPageSize());
		log.info("CORRECTED KEYWORD: [" + criteria.getKeyword() + "]");

		assertEquals(1, criteria.getPage());
		assertEquals(1, criteria.getPageSize());
		assertEquals("テスト検索語", criteria.getKeyword());
	}

	// 利用停止を含める設定値の補正を確認
	@Test
	public void testIncludeInactiveCorrection() {
		PartnerCriteria criteria = new PartnerCriteria();

		criteria.setIncludeInactive("Y");

		log.info("========== INCLUDE INACTIVE Y TEST ==========");
		log.info("INCLUDE INACTIVE: " + criteria.getIncludeInactive());

		assertEquals("Y", criteria.getIncludeInactive());

		criteria.setIncludeInactive("INVALID");

		log.info("========== INCLUDE INACTIVE INVALID TEST ==========");
		log.info("CORRECTED INCLUDE INACTIVE: " + criteria.getIncludeInactive());

		assertEquals("N", criteria.getIncludeInactive());
	}
}
