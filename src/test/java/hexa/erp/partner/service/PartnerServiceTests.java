package hexa.erp.partner.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import hexa.erp.partner.domain.PartnerCriteria;
import hexa.erp.partner.domain.PartnerVO;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    "file:src/main/webapp/WEB-INF/spring/root-context.xml"
)
@Transactional
@Rollback
@Log4j
public class PartnerServiceTests {

    @Autowired
    private PartnerService service;

    // Service 객체 주입 확인
    @Test
    public void testServiceExist() {
        log.info("========== SERVICE INJECTION TEST ==========");
        log.info("SERVICE: " + service);

        assertNotNull(service);
    }

    // Service 거래처 등록과 조회 확인
    @Test
    public void testRegisterAndGetList() {
        PartnerVO partner = createPartner();

        log.info("========== SERVICE REGISTER TEST ==========");
        log.info("REGISTER BEFORE: " + partner);

        service.register(partner);

        log.info("GENERATED PARTNER ID: " + partner.getPartnerId());
        log.info("REGISTER AFTER: " + partner);

        PartnerCriteria criteria = createCriteria(partner.getPartnerCode());
        List<PartnerVO> list = service.getList(criteria);
        int totalCount = service.getTotal(criteria);

        log.info("========== SERVICE LIST RESULT ==========");
        log.info("LIST SIZE: " + list.size());
        log.info("TOTAL COUNT: " + totalCount);
        list.forEach(row -> log.info("PARTNER ROW: " + row));

        PartnerVO savedPartner = findById(list, partner.getPartnerId());

        assertNotNull(partner.getPartnerId());
        assertNotNull(savedPartner);
        assertFalse(list.isEmpty());
        assertEquals(1, totalCount);
    }

    // Service 거래처 수정 확인
    @Test
    public void testModify() {
        PartnerVO partner = createPartner();
        service.register(partner);

        String changedName = "서비스 수정 거래처 " + createToken();
        partner.setPartnerName(changedName);

        log.info("========== SERVICE MODIFY TEST ==========");
        log.info("PARTNER ID: " + partner.getPartnerId());
        log.info("CHANGED PARTNER NAME: " + changedName);

        boolean result = service.modify(partner);

        PartnerCriteria criteria = createCriteria(changedName);

        PartnerVO changedPartner = findById(
            service.getList(criteria),
            partner.getPartnerId()
        );

        log.info("MODIFY RESULT: " + result);
        log.info("UPDATED PARTNER: " + changedPartner);

        assertTrue(result);
        assertNotNull(changedPartner);
        assertEquals(changedName, changedPartner.getPartnerName());
    }

    // Service 사용중단과 재사용 처리 확인
    @Test
    public void testChangeActive() {
        PartnerVO partner = createPartner();
        service.register(partner);

        log.info("========== SERVICE STOP PARTNER TEST ==========");
        log.info("PARTNER ID: " + partner.getPartnerId());

        int stopCount = service.changeActive(
            Collections.singletonList(partner.getPartnerId()),
            "N"
        );

        PartnerCriteria criteria = createCriteria(partner.getPartnerCode());

        PartnerVO stoppedPartner = findById(
            service.getList(criteria),
            partner.getPartnerId()
        );

        log.info("STOP COUNT: " + stopCount);
        log.info("STOPPED ACTIVE FLAG: "
            + stoppedPartner.getActiveFlag());

        assertEquals(1, stopCount);
        assertNotNull(stoppedPartner);
        assertEquals("N", stoppedPartner.getActiveFlag());

        log.info("========== SERVICE REUSE PARTNER TEST ==========");

        int reuseCount = service.changeActive(
            Collections.singletonList(partner.getPartnerId()),
            "Y"
        );

        PartnerVO reusedPartner = findById(
            service.getList(criteria),
            partner.getPartnerId()
        );

        log.info("REUSE COUNT: " + reuseCount);
        log.info("REUSED ACTIVE FLAG: "
            + reusedPartner.getActiveFlag());

        assertEquals(1, reuseCount);
        assertNotNull(reusedPartner);
        assertEquals("Y", reusedPartner.getActiveFlag());
    }

    private PartnerCriteria createCriteria(String keyword) {
        PartnerCriteria criteria = new PartnerCriteria();

        criteria.setPage(1);
        criteria.setPageSize(50);
        criteria.setKeyword(keyword);
        criteria.setIncludeInactive("Y");

        return criteria;
    }

    private PartnerVO findById(List<PartnerVO> list, Long partnerId) {
        return list.stream()
            .filter(partner -> partnerId.equals(partner.getPartnerId()))
            .findFirst()
            .orElse(null);
    }

    private PartnerVO createPartner() {
        String token = createToken();

        PartnerVO partner = new PartnerVO();
        partner.setPartnerCode("TEST" + token);
        partner.setPartnerName("서비스 테스트 거래처 " + token);
        partner.setBusinessNo("BN" + token);
        partner.setRepresentative("서비스 테스트 대표자 " + token);
        partner.setBusinessType("서비스업");
        partner.setBusinessItem("소프트웨어");
        partner.setPhone("PHONE" + token);
        partner.setMobile("MOBILE" + token);
        partner.setEmail("service" + token + "@hexa.test");
        partner.setPostalCode("12345");
        partner.setAddress("서비스 테스트 주소 " + token);
        partner.setNote("서비스 테스트 적요 " + token);

        return partner;
    }

    private String createToken() {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12);
    }
}
