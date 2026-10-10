package hexa.erp.sale.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import hexa.erp.item.domain.ItemVO;
import hexa.erp.item.mapper.ItemMapper;
import hexa.erp.sale.domain.SaleLineVO;
import hexa.erp.sale.domain.SaleVO;
import hexa.erp.sale.service.SaleService;
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
public class SaleControllerTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SaleService saleService;

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

    // 신규 판매 입력 화면 확인
    @Test
    public void testForm() throws Exception {
        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/sale/form")
            )
            .andReturn();

        Object form = result
            .getModelAndView()
            .getModelMap()
            .get("form");

        Object mode = result
            .getModelAndView()
            .getModelMap()
            .get("mode");

        log.info("========== SALE FORM TEST ==========");
        log.info(
            "HTTP STATUS: "
                + result.getResponse().getStatus()
        );
        log.info(
            "VIEW NAME: "
                + result.getModelAndView()
                    .getViewName()
        );
        log.info("FORM DATA: " + form);
        log.info("FORM MODE: " + mode);

        assertEquals(
            200,
            result.getResponse().getStatus()
        );
        assertEquals(
            "sale/form",
            result.getModelAndView().getViewName()
        );
        assertNotNull(form);
        assertEquals("create", mode);
    }

    // 신규 판매 저장 요청 확인
    @Test
    public void testSave() throws Exception {
        ItemVO item = createItem();
        itemMapper.insertSelectKey(item);

        WarehouseLookupVO warehouse =
            stockMapper.getWarehouseList().get(0);

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/sale/save")
                    .param(
                        "businessDate",
                        LocalDate.now().toString()
                    )
                    .param(
                        "warehouseId",
                        String.valueOf(
                            warehouse.getWarehouseId()
                        )
                    )
                    .param(
                        "lines[0].itemId",
                        String.valueOf(
                            item.getItemId()
                        )
                    )
                    .param(
                        "lines[0].quantity",
                        "3"
                    )
                    .param(
                        "lines[0].unitPrice",
                        "10000"
                    )
                    .param(
                        "lines[0].note",
                        "Controller 테스트"
                    )
            )
            .andReturn();

        String viewName =
            result.getModelAndView().getViewName();

        Object errorMessage =
            result.getFlashMap().get("errorMessage");

        log.info("========== SALE SAVE TEST ==========");
        log.info(
            "HTTP STATUS: "
                + result.getResponse().getStatus()
        );
        log.info("VIEW NAME: " + viewName);
        log.info(
            "ERROR MESSAGE: "
                + errorMessage
        );

        assertEquals(
            302,
            result.getResponse().getStatus()
        );
        assertEquals(
            "redirect:/sale/form",
            viewName
        );
        assertNull(errorMessage);
    }

    // 기존 판매 입력 화면 확인
    @Test
    public void testEditForm() throws Exception {
        ItemVO item = createItem();
        itemMapper.insertSelectKey(item);

        WarehouseLookupVO warehouse =
            stockMapper.getWarehouseList().get(0);

        SaleVO sale = createSale(
            item,
            warehouse
        );

        Long saleId = saleService.save(sale);

        assertNotNull(saleId);

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/sale/form")
                    .param(
                        "id",
                        String.valueOf(saleId)
                    )
            )
            .andReturn();

        SaleVO form = (SaleVO) result
            .getModelAndView()
            .getModelMap()
            .get("form");

        Object mode = result
            .getModelAndView()
            .getModelMap()
            .get("mode");

        log.info(
            "========== SALE EDIT FORM TEST =========="
        );
        log.info(
            "HTTP STATUS: "
                + result.getResponse().getStatus()
        );
        log.info("SALE ID: " + saleId);
        log.info("FORM DATA: " + form);
        log.info("FORM MODE: " + mode);

        assertEquals(
            200,
            result.getResponse().getStatus()
        );
        assertNotNull(form);
        assertEquals(
            saleId,
            form.getSaleId()
        );
        assertEquals("edit", mode);
    }

    // 판매 필수값 검증 확인
    @Test
    public void testSaveValidation()
            throws Exception {

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/sale/save")
                    .param("businessDate", "")
            )
            .andReturn();

        Object errorMessage =
            result.getFlashMap().get("errorMessage");

        log.info(
            "========== SALE VALIDATION TEST =========="
        );
        log.info(
            "HTTP STATUS: "
                + result.getResponse().getStatus()
        );
        log.info(
            "ERROR MESSAGE: "
                + errorMessage
        );

        assertEquals(
            302,
            result.getResponse().getStatus()
        );
        assertTrue(
            result.getModelAndView()
                .getViewName()
                .startsWith("redirect:/sale/form")
        );
        assertEquals(
            "판매 일자를 입력해 주세요.",
            errorMessage
        );
    }

    // 테스트 판매 생성
    private SaleVO createSale(
            ItemVO item,
            WarehouseLookupVO warehouse) {

        SaleVO sale = new SaleVO();

        sale.setBusinessDate(
            LocalDate.now().toString()
        );
        sale.setWarehouseId(
            warehouse.getWarehouseId()
        );

        SaleLineVO line = new SaleLineVO();

        line.setItemId(item.getItemId());
        line.setQuantity(
            new BigDecimal("3")
        );
        line.setUnitPrice(
            new BigDecimal("10000")
        );

        sale.getLines().add(line);

        return sale;
    }

    // 테스트 품목 생성
    private ItemVO createItem() {
        String token = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12);

        ItemVO item = new ItemVO();

        item.setItemCode(
            "SALECTRL" + token
        );
        item.setItemName(
            "판매 컨트롤러 테스트 품목 " + token
        );
        item.setSpecification("100mm");
        item.setUnit("EA");
        item.setItemType("상품");
        item.setInboundPrice(
            new BigDecimal("7000")
        );
        item.setOutboundPrice(
            new BigDecimal("10000")
        );

        return item;
    }
}
