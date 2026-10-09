package hexa.erp.item.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
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

import hexa.erp.item.domain.ItemCriteria;
import hexa.erp.item.domain.ItemVO;
import hexa.erp.item.mapper.ItemMapper;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.stock.mapper.StockMapper;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
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
public class ItemControllerTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private StockMapper stockMapper;

    private MockMvc mockMvc;

    // MockMvc 객체 생성
    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .build();
    }

    // 품목 목록과 Model 데이터 확인
    @Test
    public void testList() throws Exception {
        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/master/item")
                    .param("page", "1")
                    .param("pageSize", "50")
                    .param("keyword", "")
                    .param("includeInactive", "N")
            )
            .andReturn();

        String viewName =
            result.getModelAndView().getViewName();

        Object itemList = result
            .getModelAndView()
            .getModelMap()
            .get("itemList");

        Object warehouseOptions = result
            .getModelAndView()
            .getModelMap()
            .get("warehouseOptions");

        log.info("========== CONTROLLER LIST TEST ==========");
        log.info(
            "HTTP STATUS: "
                + result.getResponse().getStatus()
        );
        log.info("VIEW NAME: " + viewName);
        log.info("ITEM LIST: " + itemList);
        log.info(
            "WAREHOUSE OPTIONS: "
                + warehouseOptions
        );

        assertEquals(
            200,
            result.getResponse().getStatus()
        );
        assertEquals("master/item", viewName);
        assertNotNull(itemList);
        assertNotNull(warehouseOptions);
    }

    // 품목 등록 결과 확인
    @Test
    public void testRegister() throws Exception {
        ItemVO item = createItem();

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/item/save")
                    .param(
                        "itemCode",
                        item.getItemCode()
                    )
                    .param(
                        "itemName",
                        item.getItemName()
                    )
                    .param(
                        "specification",
                        item.getSpecification()
                    )
                    .param("unit", item.getUnit())
                    .param(
                        "itemType",
                        item.getItemType()
                    )
                    .param(
                        "inboundPrice",
                        item.getInboundPrice().toPlainString()
                    )
                    .param(
                        "outboundPrice",
                        item.getOutboundPrice().toPlainString()
                    )
            )
            .andReturn();

        ItemCriteria criteria =
            createCriteria(item.getItemCode());

        List<ItemVO> savedList =
            itemMapper.getListWithPaging(criteria);

        log.info("========== CONTROLLER REGISTER TEST ==========");
        log.info(
            "HTTP STATUS: "
                + result.getResponse().getStatus()
        );
        log.info(
            "VIEW NAME: "
                + result.getModelAndView().getViewName()
        );
        log.info(
            "ITEM CODE: "
                + item.getItemCode()
        );
        savedList.forEach(savedItem ->
            log.info("SAVED ITEM: " + savedItem)
        );

        assertEquals(
            302,
            result.getResponse().getStatus()
        );
        assertTrue(
            result.getModelAndView()
                .getViewName()
                .startsWith("redirect:/master/item")
        );
        assertTrue(!savedList.isEmpty());
    }

    // 품목 사용중단 결과 확인
    @Test
    public void testChangeActive() throws Exception {
        ItemVO item = createItem();
        itemMapper.insertSelectKey(item);

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/item/active")
                    .param(
                        "ids",
                        String.valueOf(item.getItemId())
                    )
                    .param("activeFlag", "N")
            )
            .andReturn();

        ItemVO changedItem =
            itemMapper.read(item.getItemId());

        log.info(
            "========== CONTROLLER ACTIVE TEST =========="
        );
        log.info(
            "HTTP STATUS: "
                + result.getResponse().getStatus()
        );
        log.info("ITEM ID: " + item.getItemId());
        log.info(
            "ACTIVE FLAG: "
                + changedItem.getActiveFlag()
        );

        assertEquals(
            302,
            result.getResponse().getStatus()
        );
        assertEquals(
            "N",
            changedItem.getActiveFlag()
        );
    }

    // 품목 재고 저장 결과 확인
    @Test
    public void testSaveStock() throws Exception {
        ItemVO item = createItem();
        itemMapper.insertSelectKey(item);

        WarehouseLookupVO warehouse =
            stockMapper.getWarehouseList().get(0);

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/item/stock")
                    .param(
                        "itemId",
                        String.valueOf(item.getItemId())
                    )
                    .param(
                        "warehouseId",
                        String.valueOf(
                            warehouse.getWarehouseId()
                        )
                    )
                    .param("quantity", "25.125")
            )
            .andReturn();

        StockVO stock = stockMapper.read(
            warehouse.getWarehouseId(),
            item.getItemId()
        );

        log.info("========== CONTROLLER STOCK TEST ==========");
        log.info(
            "HTTP STATUS: "
                + result.getResponse().getStatus()
        );
        log.info("SAVED STOCK: " + stock);

        assertEquals(
            302,
            result.getResponse().getStatus()
        );
        assertNotNull(stock);
        assertEquals(
            0,
            new BigDecimal("25.125").compareTo(
                stock.getQuantity()
            )
        );
    }

    // 품목 필수값 검증 확인
    @Test
    public void testSaveValidation() throws Exception {
        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/item/save")
                    .param("itemCode", "")
                    .param("itemName", "")
                    .param("itemType", "상품")
            )
            .andReturn();

        Object errorMessage =
            result.getFlashMap().get("errorMessage");

        log.info(
            "========== CONTROLLER VALIDATION TEST =========="
        );
        log.info(
            "HTTP STATUS: "
                + result.getResponse().getStatus()
        );
        log.info("ERROR MESSAGE: " + errorMessage);

        assertEquals(
            302,
            result.getResponse().getStatus()
        );
        assertEquals(
            "품목 코드와 품목명을 입력해 주세요.",
            errorMessage
        );
    }

    // 이미지 없는 품목 응답 확인
    @Test
    public void testImageNotFound() throws Exception {
        ItemVO item = createItem();
        itemMapper.insertSelectKey(item);

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/master/item/image")
                    .param(
                        "itemId",
                        String.valueOf(item.getItemId())
                    )
            )
            .andReturn();

        log.info(
            "========== CONTROLLER IMAGE TEST =========="
        );
        log.info(
            "HTTP STATUS: "
                + result.getResponse().getStatus()
        );

        assertEquals(
            404,
            result.getResponse().getStatus()
        );
    }

    // 검색 조건 생성
    private ItemCriteria createCriteria(String keyword) {
        ItemCriteria criteria = new ItemCriteria();

        criteria.setKeyword(keyword);
        criteria.setIncludeInactive("Y");

        return criteria;
    }

    // 테스트 품목 생성
    private ItemVO createItem() {
        String token = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12);

        ItemVO item = new ItemVO();
        item.setItemCode("CONTROLLER" + token);
        item.setItemName(
            "컨트롤러 테스트 품목 " + token
        );
        item.setSpecification("100mm");
        item.setUnit("EA");
        item.setItemType("상품");
        item.setInboundPrice(
            new BigDecimal("10000")
        );
        item.setOutboundPrice(
            new BigDecimal("15000")
        );
        item.setNote("Controller 테스트 품목");

        return item;
    }
}
