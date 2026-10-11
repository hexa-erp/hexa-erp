package hexa.erp.warehouse.mapper;

import static org.junit.Assert.assertEquals;
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

import hexa.erp.warehouse.domain.WarehouseCriteria;
import hexa.erp.warehouse.domain.WarehouseVO;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    "file:src/main/webapp/WEB-INF/spring/root-context.xml"
)
@Transactional
@Rollback
@Log4j
public class WarehouseMapperTests {

    @Autowired
    private WarehouseMapper mapper;

    // Mapper 객체 주입 확인
    @Test
    public void testMapperExist() {
        log.info("========== MAPPER INJECTION TEST ==========");
        log.info("MAPPER: " + mapper);

        assertNotNull(mapper);
    }

    // 창고 등록과 조회 확인
    @Test
    public void testInsertAndSelect() {
        WarehouseVO warehouse = createWarehouse();

        log.info("========== MAPPER INSERT TEST ==========");
        log.info("INSERT BEFORE: " + warehouse);

        mapper.insertSelectKey(warehouse);

        log.info("GENERATED WAREHOUSE ID: " + warehouse.getWarehouseId());
        log.info("INSERT AFTER: " + warehouse);

        WarehouseCriteria criteria = createCriteria(
            warehouse.getWarehouseCode()
        );

        List<WarehouseVO> list = mapper.getListWithPaging(criteria);
        int totalCount = mapper.getTotalCount(criteria);

        WarehouseVO savedWarehouse = findById(
            list,
            warehouse.getWarehouseId()
        );

        log.info("========== MAPPER SELECT RESULT ==========");
        log.info("SEARCH KEYWORD: " + criteria.getKeyword());
        log.info("LIST SIZE: " + list.size());
        log.info("TOTAL COUNT: " + totalCount);
        list.forEach(row -> log.info("WAREHOUSE ROW: " + row));

        assertNotNull(warehouse.getWarehouseId());
        assertNotNull(savedWarehouse);
        assertEquals(1, totalCount);
        assertEquals(
            warehouse.getWarehouseCode(),
            savedWarehouse.getWarehouseCode()
        );
        assertEquals(
            warehouse.getWarehouseName(),
            savedWarehouse.getWarehouseName()
        );
    }

    // 창고 정보 수정 확인
    @Test
    public void testUpdate() {
        WarehouseVO warehouse = createWarehouse();
        mapper.insertSelectKey(warehouse);

        String originalCode = warehouse.getWarehouseCode();
        String changedName = "수정 창고 " + createToken();

        warehouse.setWarehouseName(changedName);
        warehouse.setWarehouseType("공장");

        log.info("========== MAPPER UPDATE TEST ==========");
        log.info("WAREHOUSE ID: " + warehouse.getWarehouseId());
        log.info("ORIGINAL WAREHOUSE CODE: " + originalCode);
        log.info("CHANGED WAREHOUSE NAME: " + changedName);
        log.info("CHANGED WAREHOUSE TYPE: 공장");

        int updateCount = mapper.update(warehouse);

        WarehouseCriteria criteria = createCriteria(changedName);

        WarehouseVO changedWarehouse = findById(
            mapper.getListWithPaging(criteria),
            warehouse.getWarehouseId()
        );

        log.info("UPDATE COUNT: " + updateCount);
        log.info("UPDATED WAREHOUSE: " + changedWarehouse);

        assertEquals(1, updateCount);
        assertNotNull(changedWarehouse);
        assertEquals(
            originalCode,
            changedWarehouse.getWarehouseCode()
        );
        assertEquals(
            changedName,
            changedWarehouse.getWarehouseName()
        );
        assertEquals(
            "공장",
            changedWarehouse.getWarehouseType()
        );
    }

    // 창고 사용중단 처리 확인
    @Test
    public void testChangeActive() {
        WarehouseVO warehouse = createWarehouse();
        mapper.insertSelectKey(warehouse);

        log.info("========== MAPPER CHANGE ACTIVE TEST ==========");
        log.info("WAREHOUSE ID: " + warehouse.getWarehouseId());
        log.info("CHANGE ACTIVE FLAG: N");

        int changeCount = mapper.changeActive(
            Collections.singletonList(warehouse.getWarehouseId()),
            "N"
        );

        WarehouseCriteria includeInactive = createCriteria(
            warehouse.getWarehouseCode()
        );

        WarehouseVO changedWarehouse = findById(
            mapper.getListWithPaging(includeInactive),
            warehouse.getWarehouseId()
        );

        WarehouseCriteria activeOnly = createCriteria(
            warehouse.getWarehouseCode()
        );
        activeOnly.setIncludeInactive("N");

        List<WarehouseVO> activeList =
            mapper.getListWithPaging(activeOnly);

        log.info("CHANGE COUNT: " + changeCount);
        log.info(
            "CHANGED ACTIVE FLAG: "
                + changedWarehouse.getActiveFlag()
        );
        log.info("ACTIVE ONLY LIST SIZE: " + activeList.size());

        assertEquals(1, changeCount);
        assertNotNull(changedWarehouse);
        assertEquals("N", changedWarehouse.getActiveFlag());
        assertTrue(activeList.isEmpty());
    }

    // 코드·창고명·구분 검색 확인
    @Test
    public void testSearch() {
        WarehouseVO warehouse = createWarehouse();
        mapper.insertSelectKey(warehouse);

        WarehouseCriteria codeCriteria = createCriteria(
            warehouse.getWarehouseCode()
        );

        WarehouseCriteria nameCriteria = createCriteria(
            warehouse.getWarehouseName()
        );

        WarehouseCriteria typeCriteria = createCriteria(
            warehouse.getWarehouseType()
        );
        typeCriteria.setPageSize(1000);

        WarehouseVO codeResult = findById(
            mapper.getListWithPaging(codeCriteria),
            warehouse.getWarehouseId()
        );

        WarehouseVO nameResult = findById(
            mapper.getListWithPaging(nameCriteria),
            warehouse.getWarehouseId()
        );

        WarehouseVO typeResult = findById(
            mapper.getListWithPaging(typeCriteria),
            warehouse.getWarehouseId()
        );

        log.info("========== MAPPER SEARCH TEST ==========");
        log.info(
            "CODE KEYWORD: " + warehouse.getWarehouseCode()
        );
        log.info("CODE RESULT: " + codeResult);
        log.info(
            "NAME KEYWORD: " + warehouse.getWarehouseName()
        );
        log.info("NAME RESULT: " + nameResult);
        log.info(
            "TYPE KEYWORD: " + warehouse.getWarehouseType()
        );
        log.info("TYPE RESULT: " + typeResult);

        assertNotNull(codeResult);
        assertNotNull(nameResult);
        assertNotNull(typeResult);
    }

    // 검색 조건 생성
    private WarehouseCriteria createCriteria(String keyword) {
        WarehouseCriteria criteria = new WarehouseCriteria();

        criteria.setPage(1);
        criteria.setPageSize(50);
        criteria.setKeyword(keyword);
        criteria.setIncludeInactive("Y");

        return criteria;
    }

    // 창고 ID로 조회 결과 확인
    private WarehouseVO findById(
            List<WarehouseVO> list,
            Long warehouseId) {

        return list.stream()
            .filter(warehouse ->
                warehouseId.equals(warehouse.getWarehouseId()))
            .findFirst()
            .orElse(null);
    }

    // 테스트 창고 생성
    private WarehouseVO createWarehouse() {
        String token = createToken();

        WarehouseVO warehouse = new WarehouseVO();
        warehouse.setWarehouseCode("TEST" + token);
        warehouse.setWarehouseName("테스트 창고 " + token);
        warehouse.setWarehouseType("창고");

        return warehouse;
    }

    // 중복 방지 문자열 생성
    private String createToken() {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12);
    }
}
