package hexa.erp.partner.mapper;

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
public class PartnerMapperTests {

    @Autowired
    private PartnerMapper mapper;

    // Mapper 객체 주입 확인
    @Test
    public void testMapperExist() {
        log.info("========== MAPPER INJECTION TEST ==========");
        log.info("MAPPER: " + mapper);

        assertNotNull(mapper);
    }

    // 거래처 등록과 조회 확인
    @Test
    public void testInsertAndSelect() {
        PartnerVO partner = createPartner();

        log.info("========== MAPPER INSERT TEST ==========");
        log.info("INSERT BEFORE: " + partner);

        mapper.insertSelectKey(partner);

        log.info("GENERATED PARTNER ID: " + partner.getPartnerId());
        log.info("INSERT AFTER: " + partner);

        PartnerCriteria criteria = createCriteria(partner.getPartnerCode());
        List<PartnerVO> list = mapper.getListWithPaging(criteria);
        int totalCount = mapper.getTotalCount(criteria);

        log.info("========== MAPPER SELECT RESULT ==========");
        log.info("SEARCH KEYWORD: " + criteria.getKeyword());
        log.info("LIST SIZE: " + list.size());
        log.info("TOTAL COUNT: " + totalCount);
        list.forEach(row -> log.info("PARTNER ROW: " + row));

        PartnerVO savedPartner = findById(list, partner.getPartnerId());

        assertNotNull(partner.getPartnerId());
        assertNotNull(savedPartner);
        assertEquals(1, totalCount);
        assertEquals(partner.getPartnerCode(), savedPartner.getPartnerCode());
        assertEquals(partner.getPartnerName(), savedPartner.getPartnerName());
    }

    // 거래처 정보 수정 확인
    @Test
    public void testUpdate() {
        PartnerVO partner = createPartner();
        mapper.insertSelectKey(partner);

        String originalCode = partner.getPartnerCode();
        String changedName = "수정 거래처 " + createToken();

        partner.setPartnerName(changedName);
        partner.setPhone("02-9999-9999");

        log.info("========== MAPPER UPDATE TEST ==========");
        log.info("UPDATE PARTNER ID: " + partner.getPartnerId());
        log.info("ORIGINAL PARTNER CODE: " + originalCode);
        log.info("CHANGED PARTNER NAME: " + changedName);

        int updateCount = mapper.update(partner);

        PartnerCriteria criteria = createCriteria(changedName);
        List<PartnerVO> list = mapper.getListWithPaging(criteria);
        PartnerVO changedPartner = findById(list, partner.getPartnerId());

        log.info("UPDATE COUNT: " + updateCount);
        log.info("UPDATED PARTNER: " + changedPartner);

        assertEquals(1, updateCount);
        assertNotNull(changedPartner);
        assertEquals(originalCode, changedPartner.getPartnerCode());
        assertEquals(changedName, changedPartner.getPartnerName());
        assertEquals("02-9999-9999", changedPartner.getPhone());
    }

    // 거래처 사용중단 처리 확인
    @Test
    public void testChangeActive() {
        PartnerVO partner = createPartner();
        mapper.insertSelectKey(partner);

        log.info("========== MAPPER CHANGE ACTIVE TEST ==========");
        log.info("PARTNER ID: " + partner.getPartnerId());
        log.info("CHANGE ACTIVE FLAG: N");

        int changeCount = mapper.changeActive(
            Collections.singletonList(partner.getPartnerId()),
            "N"
        );

        PartnerCriteria includeInactive = createCriteria(
            partner.getPartnerCode()
        );

        PartnerVO changedPartner = findById(
            mapper.getListWithPaging(includeInactive),
            partner.getPartnerId()
        );

        PartnerCriteria activeOnly = createCriteria(partner.getPartnerCode());
        activeOnly.setIncludeInactive("N");

        List<PartnerVO> activeList = mapper.getListWithPaging(activeOnly);

        log.info("CHANGE COUNT: " + changeCount);
        log.info("CHANGED ACTIVE FLAG: "
            + changedPartner.getActiveFlag());
        log.info("ACTIVE ONLY LIST SIZE: " + activeList.size());

        assertEquals(1, changeCount);
        assertNotNull(changedPartner);
        assertEquals("N", changedPartner.getActiveFlag());
        assertTrue(activeList.isEmpty());
    }

    // 코드·거래처명·대표자명 검색 확인
    @Test
    public void testSearch() {
        PartnerVO partner = createPartner();
        mapper.insertSelectKey(partner);

        PartnerCriteria codeCriteria = createCriteria(
            partner.getPartnerCode()
        );

        PartnerCriteria nameCriteria = createCriteria(
            partner.getPartnerName()
        );

        PartnerCriteria representativeCriteria = createCriteria(
            partner.getRepresentative()
        );

        List<PartnerVO> codeResult =
            mapper.getListWithPaging(codeCriteria);

        List<PartnerVO> nameResult =
            mapper.getListWithPaging(nameCriteria);

        List<PartnerVO> representativeResult =
            mapper.getListWithPaging(representativeCriteria);

        log.info("========== MAPPER SEARCH TEST ==========");
        log.info("CODE KEYWORD: " + partner.getPartnerCode());
        log.info("CODE RESULT SIZE: " + codeResult.size());
        log.info("NAME KEYWORD: " + partner.getPartnerName());
        log.info("NAME RESULT SIZE: " + nameResult.size());
        log.info("REPRESENTATIVE KEYWORD: "
            + partner.getRepresentative());
        log.info("REPRESENTATIVE RESULT SIZE: "
            + representativeResult.size());

        codeResult.forEach(row ->
            log.info("CODE SEARCH ROW: " + row));

        nameResult.forEach(row ->
            log.info("NAME SEARCH ROW: " + row));

        representativeResult.forEach(row ->
            log.info("REPRESENTATIVE SEARCH ROW: " + row));

        assertFalse(codeResult.isEmpty());
        assertFalse(nameResult.isEmpty());
        assertFalse(representativeResult.isEmpty());
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
        partner.setPartnerName("테스트 거래처 " + token);
        partner.setBusinessNo("BN" + token);
        partner.setRepresentative("테스트 대표자 " + token);
        partner.setBusinessType("서비스업");
        partner.setBusinessItem("소프트웨어");
        partner.setPhone("PHONE" + token);
        partner.setMobile("MOBILE" + token);
        partner.setEmail("test" + token + "@hexa.test");
        partner.setPostalCode("12345");
        partner.setAddress("테스트 주소 " + token);
        partner.setNote("테스트 적요 " + token);

        return partner;
    }

    private String createToken() {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12);
    }
}
