package hexa.erp.shipment.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import lombok.extern.log4j.Log4j;

@Log4j
public class ShipmentCriteriaTests {

	// 기본값 확인
	@Test
	public void testDefaultValues() {
		ShipmentCriteria criteria = new ShipmentCriteria();

		log.info("========== CRITERIA DEFAULT TEST ==========");
		log.info("DEFAULT PAGE: " + criteria.getPage());
		log.info("DEFAULT PAGE SIZE: " + criteria.getPageSize());

		assertEquals(1, criteria.getPage());
		assertEquals(25, criteria.getPageSize());
		assertEquals("", criteria.getKeyword());
		assertEquals("", criteria.getProgressStatus());
	}

	// 검색값 보정 확인
	@Test
	public void testValueCorrection() {
		ShipmentCriteria criteria = new ShipmentCriteria();

		criteria.setPage(0);
		criteria.setPageSize(-10);
		criteria.setKeyword("  거래처 검색  ");
		criteria.setProgressStatus("  CONFIRMED  ");

		log.info("========== CRITERIA CORRECTION TEST ==========");
		log.info("CORRECTED PAGE: " + criteria.getPage());
		log.info("CORRECTED PAGE SIZE: " + criteria.getPageSize());
		log.info("CORRECTED KEYWORD: " + criteria.getKeyword());
		log.info("CORRECTED STATUS: " + criteria.getProgressStatus());

		assertEquals(1, criteria.getPage());
		assertEquals(1, criteria.getPageSize());
		assertEquals("거래처 검색", criteria.getKeyword());
		assertEquals("CONFIRMED", criteria.getProgressStatus());
	}

	// null 값 보정 확인
	@Test
	public void testNullCorrection() {
		ShipmentCriteria criteria = new ShipmentCriteria();

		criteria.setKeyword(null);
		criteria.setProgressStatus(null);

		log.info("========== CRITERIA NULL TEST ==========");
		log.info("NULL KEYWORD RESULT: '" + criteria.getKeyword() + "'");
		log.info("NULL STATUS RESULT: '" + criteria.getProgressStatus() + "'");

		assertEquals("", criteria.getKeyword());
		assertEquals("", criteria.getProgressStatus());
	}
}
