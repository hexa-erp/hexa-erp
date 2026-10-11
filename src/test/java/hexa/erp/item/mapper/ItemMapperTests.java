package hexa.erp.item.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
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

import hexa.erp.item.domain.ItemCriteria;
import hexa.erp.item.domain.ItemVO;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    "file:src/main/webapp/WEB-INF/spring/root-context.xml"
)
@Transactional
@Rollback
@Log4j
public class ItemMapperTests {

    @Autowired
    private ItemMapper mapper;

    // Mapper 객체 주입 확인
    @Test
    public void testMapperExist() {
        log.info("========== MAPPER INJECTION TEST ==========");
        log.info("MAPPER: " + mapper);

        assertNotNull(mapper);
    }

    // 품목 등록과 조회 확인
    @Test
    public void testInsertAndRead() {
        ItemVO item = createItem();

        log.info("========== MAPPER INSERT TEST ==========");
        log.info("INSERT BEFORE: " + item);

        mapper.insertSelectKey(item);

        log.info("GENERATED ITEM ID: " + item.getItemId());
        log.info("INSERT AFTER: " + item);

        ItemVO savedItem = mapper.read(item.getItemId());

        log.info("========== MAPPER READ RESULT ==========");
        log.info("SAVED ITEM: " + savedItem);

        assertNotNull(item.getItemId());
        assertNotNull(savedItem);
        assertEquals(item.getItemCode(), savedItem.getItemCode());
        assertEquals(item.getItemName(), savedItem.getItemName());
        assertEquals("Y", savedItem.getActiveFlag());
    }

    // 품목 정보 수정 확인
    @Test
    public void testUpdate() {
        ItemVO item = createItem();
        mapper.insertSelectKey(item);

        String originalCode = item.getItemCode();
        String changedName = "수정 품목 " + createToken();

        item.setItemName(changedName);
        item.setSpecification("수정 규격");
        item.setOutboundPrice(new BigDecimal("25000"));

        log.info("========== MAPPER UPDATE TEST ==========");
        log.info("ITEM ID: " + item.getItemId());
        log.info("ORIGINAL ITEM CODE: " + originalCode);
        log.info("CHANGED ITEM NAME: " + changedName);
        log.info(
            "CHANGED OUTBOUND PRICE: "
                + item.getOutboundPrice()
        );

        int updateCount = mapper.update(item);
        ItemVO changedItem = mapper.read(item.getItemId());

        log.info("UPDATE COUNT: " + updateCount);
        log.info("UPDATED ITEM: " + changedItem);

        assertEquals(1, updateCount);
        assertNotNull(changedItem);
        assertEquals(originalCode, changedItem.getItemCode());
        assertEquals(changedName, changedItem.getItemName());
        assertEquals(
            0,
            new BigDecimal("25000").compareTo(
                changedItem.getOutboundPrice()
            )
        );
    }

    // 품목 사용중단 처리 확인
    @Test
    public void testChangeActive() {
        ItemVO item = createItem();
        mapper.insertSelectKey(item);

        log.info("========== MAPPER CHANGE ACTIVE TEST ==========");
        log.info("ITEM ID: " + item.getItemId());
        log.info("CHANGE ACTIVE FLAG: N");

        int changeCount = mapper.changeActive(
            Collections.singletonList(item.getItemId()),
            "N"
        );

        ItemVO changedItem = mapper.read(item.getItemId());

        ItemCriteria activeOnly = createCriteria(
            item.getItemCode()
        );
        activeOnly.setIncludeInactive("N");

        List<ItemVO> activeList =
            mapper.getListWithPaging(activeOnly);

        log.info("CHANGE COUNT: " + changeCount);
        log.info(
            "CHANGED ACTIVE FLAG: "
                + changedItem.getActiveFlag()
        );
        log.info("ACTIVE ONLY LIST SIZE: " + activeList.size());

        assertEquals(1, changeCount);
        assertEquals("N", changedItem.getActiveFlag());
        assertTrue(activeList.isEmpty());
    }

    // 품목코드와 품목명 검색 확인
    @Test
    public void testSearch() {
        ItemVO item = createItem();
        mapper.insertSelectKey(item);

        ItemCriteria codeCriteria = createCriteria(
            item.getItemCode()
        );

        ItemCriteria nameCriteria = createCriteria(
            item.getItemName()
        );

        ItemVO codeResult = findById(
            mapper.getListWithPaging(codeCriteria),
            item.getItemId()
        );

        ItemVO nameResult = findById(
            mapper.getListWithPaging(nameCriteria),
            item.getItemId()
        );

        log.info("========== MAPPER SEARCH TEST ==========");
        log.info("CODE KEYWORD: " + item.getItemCode());
        log.info("CODE RESULT: " + codeResult);
        log.info("NAME KEYWORD: " + item.getItemName());
        log.info("NAME RESULT: " + nameResult);

        assertNotNull(codeResult);
        assertNotNull(nameResult);
    }

    // 검색 조건 생성
    private ItemCriteria createCriteria(String keyword) {
        ItemCriteria criteria = new ItemCriteria();

        criteria.setPage(1);
        criteria.setPageSize(50);
        criteria.setKeyword(keyword);
        criteria.setIncludeInactive("Y");

        return criteria;
    }

    // 품목 ID로 조회 결과 확인
    private ItemVO findById(
            List<ItemVO> list,
            Long itemId) {

        return list.stream()
            .filter(item -> itemId.equals(item.getItemId()))
            .findFirst()
            .orElse(null);
    }

    // 테스트 품목 생성
    private ItemVO createItem() {
        String token = createToken();

        ItemVO item = new ItemVO();
        item.setItemCode("TEST" + token);
        item.setItemName("테스트 품목 " + token);
        item.setSpecification("100mm");
        item.setUnit("EA");
        item.setItemType("상품");
        item.setInboundPrice(new BigDecimal("10000"));
        item.setOutboundPrice(new BigDecimal("15000"));
        item.setNote("Mapper 테스트 품목");

        return item;
    }

    // 중복 방지 문자열 생성
    private String createToken() {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12);
    }
}
