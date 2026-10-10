package hexa.erp.warehouse.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import lombok.extern.log4j.Log4j;

@Log4j
public class WarehouseCriteriaTests {

    // 기본 검색 조건 확인
    @Test
    public void testDefaultValues() {
        WarehouseCriteria criteria = new WarehouseCriteria();

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

    // 페이지와 검색어 보정 확인
    @Test
    public void testValueCorrection() {
        WarehouseCriteria criteria = new WarehouseCriteria();

        criteria.setPage(0);
        criteria.setPageSize(0);
        criteria.setKeyword("  중앙창고  ");

        log.info("========== CRITERIA CORRECTION TEST ==========");
        log.info("CORRECTED PAGE: " + criteria.getPage());
        log.info("CORRECTED PAGE SIZE: " + criteria.getPageSize());
        log.info("CORRECTED KEYWORD: [" + criteria.getKeyword() + "]");

        assertEquals(1, criteria.getPage());
        assertEquals(1, criteria.getPageSize());
        assertEquals("중앙창고", criteria.getKeyword());
    }

    // 사용중단 포함 값 보정 확인
    @Test
    public void testIncludeInactiveCorrection() {
        WarehouseCriteria criteria = new WarehouseCriteria();

        criteria.setIncludeInactive("Y");

        log.info("========== INCLUDE INACTIVE Y TEST ==========");
        log.info("INCLUDE INACTIVE: " + criteria.getIncludeInactive());

        assertEquals("Y", criteria.getIncludeInactive());

        criteria.setIncludeInactive("INVALID");

        log.info("========== INCLUDE INACTIVE INVALID TEST ==========");
        log.info(
            "CORRECTED INCLUDE INACTIVE: "
                + criteria.getIncludeInactive()
        );

        assertEquals("N", criteria.getIncludeInactive());
    }
}
