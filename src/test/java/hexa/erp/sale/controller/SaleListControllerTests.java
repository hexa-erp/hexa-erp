package hexa.erp.sale.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
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
public class SaleListControllerTests {

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

    // 판매 목록 화면 확인
    @Test
    public void testList()
            throws Exception {

        String token = createToken();

        SaleVO sale = saveSale(token);

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/sale/list")
                    .param(
                        "keyword",
                        token
                    )
                    .param(
                        "progressStatus",
                        "CONFIRMED"
                    )
            )
            .andReturn();

        Object saleList =
            result.getModelAndView()
                .getModelMap()
                .get("saleList");

        Object totalCount =
            result.getModelAndView()
                .getModelMap()
                .get("totalCount");

        log.info(
            "========== SALE LIST CONTROLLER TEST =========="
        );
        log.info(
            "HTTP STATUS: "
                + result.getResponse()
                    .getStatus()
        );
        log.info(
            "VIEW NAME: "
                + result.getModelAndView()
                    .getViewName()
        );
        log.info(
            "SAVED SALE ID: "
                + sale.getSaleId()
        );
        log.info(
            "SALE LIST: "
                + saleList
        );
        log.info(
            "TOTAL COUNT: "
                + totalCount
        );

        assertEquals(
            200,
            result.getResponse()
                .getStatus()
        );

        assertEquals(
            "sale/list",
            result.getModelAndView()
                .getViewName()
        );

        assertNotNull(saleList);
        assertEquals(1, totalCount);
    }

    // 거래명세서 화면 확인
    @SuppressWarnings("unchecked")
    @Test
    public void testStatement()
            throws Exception {

        SaleVO sale =
            saveSale(createToken());

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/sale/statement")
                    .param(
                        "id",
                        String.valueOf(
                            sale.getSaleId()
                        )
                    )
            )
            .andReturn();

        SaleVO form =
            (SaleVO) result
                .getModelAndView()
                .getModelMap()
                .get("form");

        Map<String, BigDecimal> totals =
            (Map<String, BigDecimal>) result
                .getModelAndView()
                .getModelMap()
                .get("statementTotals");

        log.info(
            "========== SALE STATEMENT TEST =========="
        );
        log.info(
            "HTTP STATUS: "
                + result.getResponse()
                    .getStatus()
        );
        log.info(
            "VIEW NAME: "
                + result.getModelAndView()
                    .getViewName()
        );
        log.info("FORM: " + form);
        log.info("TOTALS: " + totals);

        assertEquals(
            200,
            result.getResponse()
                .getStatus()
        );

        assertEquals(
            "sale/statement",
            result.getModelAndView()
                .getViewName()
        );

        assertNotNull(form);
        assertNotNull(totals);

        assertEquals(
            0,
            new BigDecimal("33000")
                .compareTo(
                    totals.get(
                        "totalAmount"
                    )
                )
        );
    }

    // 진행상태 변경 요청 확인
    @Test
    public void testChangeStatus()
            throws Exception {

        SaleVO sale =
            saveSale(createToken());

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post(
                        "/sale/change-status"
                    )
                    .param(
                        "selectedIds",
                        String.valueOf(
                            sale.getSaleId()
                        )
                    )
                    .param(
                        "nextProgressStatus",
                        "UNCONFIRMED"
                    )
            )
            .andReturn();

        SaleVO changedSale =
            saleService.get(
                sale.getSaleId()
            );

        String viewName =
            result.getModelAndView()
                .getViewName();

        log.info(
            "========== STATUS CONTROLLER TEST =========="
        );
        log.info(
            "VIEW NAME: "
                + viewName
        );
        log.info(
            "CHANGED STATUS: "
                + changedSale
                    .getProgressStatus()
        );

        assertTrue(
            viewName.startsWith(
                "redirect:/sale/list"
            )
        );

        assertEquals(
            "UNCONFIRMED",
            changedSale
                .getProgressStatus()
        );
    }

    // 판매 삭제 요청 확인
    @Test
    public void testDelete()
            throws Exception {

        SaleVO sale =
            saveSale(createToken());

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/sale/delete")
                    .param(
                        "selectedIds",
                        String.valueOf(
                            sale.getSaleId()
                        )
                    )
            )
            .andReturn();

        SaleVO removedSale =
            saleService.get(
                sale.getSaleId()
            );

        String viewName =
            result.getModelAndView()
                .getViewName();

        log.info(
            "========== DELETE CONTROLLER TEST =========="
        );
        log.info(
            "VIEW NAME: "
                + viewName
        );
        log.info(
            "REMOVED SALE: "
                + removedSale
        );

        assertTrue(
            viewName.startsWith(
                "redirect:/sale/list"
            )
        );

        assertNull(removedSale);
    }

    // 테스트 판매 저장
    private SaleVO saveSale(String token) {
        ItemVO item = createItem(token);

        itemMapper.insertSelectKey(item);

        WarehouseLookupVO warehouse =
            stockMapper
                .getWarehouseList()
                .get(0);

        SaleVO sale = new SaleVO();

        sale.setBusinessDate(
            LocalDate.now().toString()
        );

        sale.setPartnerName(
            "Controller 조회 거래처 "
                + token
        );

        sale.setWarehouseId(
            warehouse.getWarehouseId()
        );

        SaleLineVO line =
            new SaleLineVO();

        line.setItemId(
            item.getItemId()
        );

        line.setQuantity(
            new BigDecimal("3")
        );

        line.setUnitPrice(
            new BigDecimal("10000")
        );

        sale.getLines().add(line);

        Long saleId =
            saleService.save(sale);

        assertNotNull(saleId);

        return saleService.get(saleId);
    }

    // 테스트 품목 생성
    private ItemVO createItem(
            String token) {

        ItemVO item = new ItemVO();

        item.setItemCode(
            "SLISTCTRL" + token
        );

        item.setItemName(
            "Controller 조회 품목 "
                + token
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

        item.setNote(
            "판매 조회 Controller 테스트"
        );

        return item;
    }

    // 테스트 구분값 생성
    private String createToken() {
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 8);
    }
}
