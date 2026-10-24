package hexa.erp.sale.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
@ContextConfiguration({ "file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
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

	// MockMvcオブジェクトを生成
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	// 新規売上入力画面を確認
	@Test
	public void testForm() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/sale/form")).andReturn();

		Object form = result.getModelAndView().getModelMap().get("form");

		Object mode = result.getModelAndView().getModelMap().get("mode");

		log.info("========== SALE FORM TEST ==========");
		log.info("HTTP STATUS: " + result.getResponse().getStatus());
		log.info("VIEW NAME: " + result.getModelAndView().getViewName());
		log.info("FORM DATA: " + form);
		log.info("FORM MODE: " + mode);

		assertEquals(200, result.getResponse().getStatus());
		assertEquals("sale/form", result.getModelAndView().getViewName());
		assertNotNull(form);
		assertEquals("create", mode);
	}

	// 既存の売上入力画面を確認
	@Test
	public void testEditForm() throws Exception {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseLookupVO warehouse = stockMapper.getWarehouseList().get(0);

		SaleVO sale = createSale(item, warehouse);

		Long saleId = saleService.save(sale);

		assertNotNull(saleId);

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/sale/form").param("id", String.valueOf(saleId)))
				.andReturn();

		SaleVO form = (SaleVO) result.getModelAndView().getModelMap().get("form");

		Object mode = result.getModelAndView().getModelMap().get("mode");

		log.info("========== SALE EDIT FORM TEST ==========");
		log.info("HTTP STATUS: " + result.getResponse().getStatus());
		log.info("SALE ID: " + saleId);
		log.info("FORM DATA: " + form);
		log.info("FORM MODE: " + mode);

		assertEquals(200, result.getResponse().getStatus());
		assertNotNull(form);
		assertEquals(saleId, form.getSaleId());
		assertEquals("edit", mode);
	}

	// テスト売上を生成
	private SaleVO createSale(ItemVO item, WarehouseLookupVO warehouse) {

		SaleVO sale = new SaleVO();

		sale.setBusinessDate(LocalDate.now().toString());
		sale.setWarehouseId(warehouse.getWarehouseId());

		SaleLineVO line = new SaleLineVO();

		line.setItemId(item.getItemId());
		line.setQuantity(new BigDecimal("3"));
		line.setUnitPrice(new BigDecimal("10000"));

		sale.getLines().add(line);

		return sale;
	}

	// テスト品目を生成
	private ItemVO createItem() {
		String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		ItemVO item = new ItemVO();

		item.setItemCode("SALECTRL" + token);
		item.setItemName("売上Controllerテスト品目 " + token);
		item.setSpecification("100mm");
		item.setUnit("EA");
		item.setItemType("商品");
		item.setInboundPrice(new BigDecimal("7000"));
		item.setOutboundPrice(new BigDecimal("10000"));

		return item;
	}
}
