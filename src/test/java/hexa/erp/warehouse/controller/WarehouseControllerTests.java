package hexa.erp.warehouse.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import hexa.erp.warehouse.domain.WarehouseCriteria;
import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.mapper.WarehouseMapper;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({
    "file:src/main/webapp/WEB-INF/spring/root-context.xml",
    "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml"
})
@Transactional
@Rollback
@Log4j
public class WarehouseControllerTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private WarehouseMapper mapper;

    private MockMvc mockMvc;

    // MockMvc 객체 생성
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .build();
    }

    // 창고 목록과 Model 데이터 확인
    @Test
    public void testList() throws Exception {
        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/master/warehouse")
                    .param("page", "1")
                    .param("pageSize", "50")
                    .param("keyword", "")
                    .param("includeInactive", "N")
            )
            .andReturn();

        String viewName = result.getModelAndView().getViewName();
        Object warehouseList = result.getModelAndView()
            .getModelMap()
            .get("warehouseList");

        log.info("========== CONTROLLER LIST TEST ==========");
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info("VIEW NAME: " + viewName);
        log.info(
            "MODEL MAP: "
                + result.getModelAndView().getModelMap()
        );
        log.info("WAREHOUSE LIST: " + warehouseList);

        assertEquals(200, result.getResponse().getStatus());
        assertEquals("master/warehouse", viewName);
        assertNotNull(warehouseList);
    }

    // 창고명 검색 결과 확인
    @Test
    public void testSearch() throws Exception {
        WarehouseVO warehouse = createWarehouse();
        mapper.insertSelectKey(warehouse);

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/master/warehouse")
                    .param("page", "1")
                    .param("pageSize", "50")
                    .param("keyword", warehouse.getWarehouseName())
                    .param("includeInactive", "Y")
            )
            .andReturn();

        List<WarehouseVO> warehouseList = getWarehouseList(result);

        WarehouseVO searchedWarehouse = findById(
            warehouseList,
            warehouse.getWarehouseId()
        );

        log.info("========== CONTROLLER SEARCH TEST ==========");
        log.info("SEARCH KEYWORD: " + warehouse.getWarehouseName());
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info("LIST SIZE: " + warehouseList.size());
        log.info("SEARCHED WAREHOUSE: " + searchedWarehouse);

        assertEquals(200, result.getResponse().getStatus());
        assertNotNull(searchedWarehouse);
    }

    // 창고 등록과 DB 저장 결과 확인
    @Test
    public void testRegister() throws Exception {
        WarehouseVO warehouse = createWarehouse();

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/warehouse/save")
                    .param(
                        "warehouseCode",
                        warehouse.getWarehouseCode()
                    )
                    .param(
                        "warehouseName",
                        warehouse.getWarehouseName()
                    )
                    .param(
                        "warehouseType",
                        warehouse.getWarehouseType()
                    )
            )
            .andReturn();

        String viewName = result.getModelAndView().getViewName();

        WarehouseCriteria criteria = createCriteria(
            warehouse.getWarehouseCode()
        );

        List<WarehouseVO> savedList =
            mapper.getListWithPaging(criteria);

        log.info("========== CONTROLLER REGISTER TEST ==========");
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info("VIEW NAME: " + viewName);
        log.info("WAREHOUSE CODE: " + warehouse.getWarehouseCode());
        log.info("SAVED LIST SIZE: " + savedList.size());
        savedList.forEach(row ->
            log.info("SAVED WAREHOUSE: " + row)
        );

        assertEquals(302, result.getResponse().getStatus());
        assertTrue(
            viewName.startsWith("redirect:/master/warehouse")
        );
        assertFalse(savedList.isEmpty());
    }

    // 창고 수정과 DB 변경 결과 확인
    @Test
    public void testModify() throws Exception {
        WarehouseVO warehouse = createWarehouse();
        mapper.insertSelectKey(warehouse);

        String changedName = "컨트롤러 수정 창고 " + createToken();

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/warehouse/save")
                    .param(
                        "warehouseId",
                        String.valueOf(warehouse.getWarehouseId())
                    )
                    .param(
                        "warehouseCode",
                        warehouse.getWarehouseCode()
                    )
                    .param("warehouseName", changedName)
                    .param("warehouseType", "공장")
            )
            .andReturn();

        WarehouseCriteria criteria = createCriteria(changedName);

        WarehouseVO changedWarehouse = findById(
            mapper.getListWithPaging(criteria),
            warehouse.getWarehouseId()
        );

        log.info("========== CONTROLLER MODIFY TEST ==========");
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info(
            "VIEW NAME: "
                + result.getModelAndView().getViewName()
        );
        log.info("WAREHOUSE ID: " + warehouse.getWarehouseId());
        log.info("CHANGED WAREHOUSE: " + changedWarehouse);

        assertEquals(302, result.getResponse().getStatus());
        assertNotNull(changedWarehouse);
        assertEquals(
            changedName,
            changedWarehouse.getWarehouseName()
        );
        assertEquals(
            "공장",
            changedWarehouse.getWarehouseType()
        );
    }

    // 창고 사용중단 결과 확인
    @Test
    public void testChangeActive() throws Exception {
        WarehouseVO warehouse = createWarehouse();
        mapper.insertSelectKey(warehouse);

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/warehouse/active")
                    .param(
                        "ids",
                        String.valueOf(warehouse.getWarehouseId())
                    )
                    .param("activeFlag", "N")
            )
            .andReturn();

        WarehouseCriteria criteria = createCriteria(
            warehouse.getWarehouseCode()
        );

        WarehouseVO changedWarehouse = findById(
            mapper.getListWithPaging(criteria),
            warehouse.getWarehouseId()
        );

        log.info(
            "========== CONTROLLER CHANGE ACTIVE TEST =========="
        );
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info(
            "VIEW NAME: "
                + result.getModelAndView().getViewName()
        );
        log.info("WAREHOUSE ID: " + warehouse.getWarehouseId());
        log.info("ACTIVE FLAG: " + changedWarehouse.getActiveFlag());

        assertEquals(302, result.getResponse().getStatus());
        assertNotNull(changedWarehouse);
        assertEquals("N", changedWarehouse.getActiveFlag());
    }

    // 필수값 누락 메시지 확인
    @Test
    public void testSaveValidation() throws Exception {
        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/warehouse/save")
                    .param("warehouseCode", "")
                    .param("warehouseName", "")
                    .param("warehouseType", "창고")
            )
            .andReturn();

        String viewName = result.getModelAndView().getViewName();
        Object errorMessage =
            result.getFlashMap().get("errorMessage");

        log.info("========== CONTROLLER VALIDATION TEST ==========");
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info("VIEW NAME: " + viewName);
        log.info("ERROR MESSAGE: " + errorMessage);

        assertEquals(302, result.getResponse().getStatus());
        assertTrue(
            viewName.startsWith("redirect:/master/warehouse")
        );
        assertEquals(
            "창고 코드와 창고명을 입력해 주세요.",
            errorMessage
        );
    }

    // 창고 구분 검증 메시지 확인
    @Test
    public void testWarehouseTypeValidation() throws Exception {
        String token = createToken();

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/warehouse/save")
                    .param("warehouseCode", "TEST" + token)
                    .param(
                        "warehouseName",
                        "구분 검증 창고 " + token
                    )
                    .param("warehouseType", "INVALID")
            )
            .andReturn();

        Object errorMessage =
            result.getFlashMap().get("errorMessage");

        log.info(
            "========== WAREHOUSE TYPE VALIDATION TEST =========="
        );
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info("ERROR MESSAGE: " + errorMessage);

        assertEquals(302, result.getResponse().getStatus());
        assertEquals(
            "창고 구분을 선택해 주세요.",
            errorMessage
        );
    }

    @SuppressWarnings("unchecked")
    private List<WarehouseVO> getWarehouseList(MvcResult result) {
        return (List<WarehouseVO>) result
            .getModelAndView()
            .getModelMap()
            .get("warehouseList");
    }

    private WarehouseCriteria createCriteria(String keyword) {
        WarehouseCriteria criteria = new WarehouseCriteria();

        criteria.setPage(1);
        criteria.setPageSize(50);
        criteria.setKeyword(keyword);
        criteria.setIncludeInactive("Y");

        return criteria;
    }

    private WarehouseVO findById(
            List<WarehouseVO> list,
            Long warehouseId) {

        return list.stream()
            .filter(warehouse ->
                warehouseId.equals(warehouse.getWarehouseId()))
            .findFirst()
            .orElse(null);
    }

    private WarehouseVO createWarehouse() {
        String token = createToken();

        WarehouseVO warehouse = new WarehouseVO();
        warehouse.setWarehouseCode("TEST" + token);
        warehouse.setWarehouseName(
            "컨트롤러 테스트 창고 " + token
        );
        warehouse.setWarehouseType("창고");

        return warehouse;
    }

    private String createToken() {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12);
    }
}
