package hexa.erp.shipment.controller;

import static org.junit.Assert.assertEquals;
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
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
@Transactional
@Rollback
@Log4j
public class ShipmentControllerTests {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	// MockMvcオブジェクトを生成
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	// 検索結果がない出荷一覧画面を確認
	@Test
	public void testList() throws Exception {
		String keyword = UUID.randomUUID().toString().replace("-", "");

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/shipment/list").param("keyword", keyword))
				.andReturn();

		assertEquals(200, result.getResponse().getStatus());

		ModelAndView modelAndView = result.getModelAndView();
		assertNotNull(modelAndView);
		assertEquals("shipment/list", modelAndView.getViewName());

		List<?> documentList = (List<?>) modelAndView.getModelMap().get("shipmentList");

		log.info("========== SHIPMENT LIST CONTROLLER TEST ==========");
		log.info("VIEW NAME: " + modelAndView.getViewName());
		log.info("DOCUMENT LIST: " + documentList);
		log.info("TOTAL COUNT: " + modelAndView.getModelMap().get("totalCount"));

		assertNotNull(documentList);
		assertTrue(documentList.isEmpty());
		assertEquals(0, modelAndView.getModelMap().get("totalCount"));
	}
}
