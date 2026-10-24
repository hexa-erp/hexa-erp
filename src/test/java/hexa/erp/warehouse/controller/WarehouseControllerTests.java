package hexa.erp.warehouse.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.mapper.WarehouseMapper;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
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
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	// 창고 목록과 Model 데이터 확인
	@Test
	public void testList() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/master/warehouse").param("page", "1")
				.param("pageSize", "50").param("keyword", "").param("includeInactive", "N")).andReturn();

		String viewName = result.getModelAndView().getViewName();
		Object warehouseList = result.getModelAndView().getModelMap().get("warehouseList");

		log.info("========== CONTROLLER LIST TEST ==========");
		log.info("HTTP STATUS: " + result.getResponse().getStatus());
		log.info("VIEW NAME: " + viewName);
		log.info("MODEL MAP: " + result.getModelAndView().getModelMap());
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

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/master/warehouse").param("page", "1")
				.param("pageSize", "50").param("keyword", warehouse.getWarehouseName()).param("includeInactive", "Y"))
				.andReturn();

		List<WarehouseVO> warehouseList = getWarehouseList(result);

		WarehouseVO searchedWarehouse = findById(warehouseList, warehouse.getWarehouseId());

		log.info("========== CONTROLLER SEARCH TEST ==========");
		log.info("SEARCH KEYWORD: " + warehouse.getWarehouseName());
		log.info("HTTP STATUS: " + result.getResponse().getStatus());
		log.info("LIST SIZE: " + warehouseList.size());
		log.info("SEARCHED WAREHOUSE: " + searchedWarehouse);

		assertEquals(200, result.getResponse().getStatus());
		assertNotNull(searchedWarehouse);
	}

	@SuppressWarnings("unchecked")
	private List<WarehouseVO> getWarehouseList(MvcResult result) {
		return (List<WarehouseVO>) result.getModelAndView().getModelMap().get("warehouseList");
	}

	private WarehouseVO findById(List<WarehouseVO> list, Long warehouseId) {

		return list.stream().filter(warehouse -> warehouseId.equals(warehouse.getWarehouseId())).findFirst()
				.orElse(null);
	}

	private WarehouseVO createWarehouse() {
		String token = createToken();

		WarehouseVO warehouse = new WarehouseVO();
		warehouse.setWarehouseCode("TEST" + token);
		warehouse.setWarehouseName("컨트롤러 테스트 창고 " + token);
		warehouse.setWarehouseType("창고");

		return warehouse;
	}

	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}
