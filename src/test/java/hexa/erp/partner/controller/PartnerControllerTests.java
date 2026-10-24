package hexa.erp.partner.controller;

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

import hexa.erp.partner.domain.PartnerVO;
import hexa.erp.partner.mapper.PartnerMapper;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
@Transactional
@Rollback
@Log4j
public class PartnerControllerTests {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private PartnerMapper mapper;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	// 거래처 목록 요청과 Model 데이터 확인
	@Test
	public void testList() throws Exception {
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/master/partner").param("page", "1")
				.param("pageSize", "50").param("keyword", "").param("includeInactive", "N")).andReturn();

		String viewName = result.getModelAndView().getViewName();
		Object partnerList = result.getModelAndView().getModelMap().get("partnerList");

		log.info("========== CONTROLLER LIST TEST ==========");
		log.info("HTTP STATUS: " + result.getResponse().getStatus());
		log.info("VIEW NAME: " + viewName);
		log.info("MODEL MAP: " + result.getModelAndView().getModelMap());
		log.info("PARTNER LIST: " + partnerList);

		assertEquals(200, result.getResponse().getStatus());
		assertEquals("master/partner", viewName);
		assertNotNull(partnerList);
	}

	// 대표자명 검색 요청 결과 확인
	@Test
	public void testSearch() throws Exception {
		PartnerVO partner = createPartner();
		mapper.insertSelectKey(partner);

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/master/partner").param("page", "1")
				.param("pageSize", "50").param("keyword", partner.getRepresentative()).param("includeInactive", "Y"))
				.andReturn();

		List<PartnerVO> partnerList = getPartnerList(result);
		PartnerVO searchedPartner = findById(partnerList, partner.getPartnerId());

		log.info("========== CONTROLLER SEARCH TEST ==========");
		log.info("SEARCH KEYWORD: " + partner.getRepresentative());
		log.info("HTTP STATUS: " + result.getResponse().getStatus());
		log.info("LIST SIZE: " + partnerList.size());
		log.info("SEARCHED PARTNER: " + searchedPartner);

		assertEquals(200, result.getResponse().getStatus());
		assertNotNull(searchedPartner);
	}

	@SuppressWarnings("unchecked")
	private List<PartnerVO> getPartnerList(MvcResult result) {
		return (List<PartnerVO>) result.getModelAndView().getModelMap().get("partnerList");
	}

	private PartnerVO findById(List<PartnerVO> list, Long partnerId) {
		return list.stream().filter(partner -> partnerId.equals(partner.getPartnerId())).findFirst().orElse(null);
	}

	private PartnerVO createPartner() {
		String token = createToken();

		PartnerVO partner = new PartnerVO();
		partner.setPartnerCode("TEST" + token);
		partner.setPartnerName("컨트롤러 테스트 거래처 " + token);
		partner.setBusinessNo("BN" + token);
		partner.setRepresentative("컨트롤러 테스트 대표자 " + token);
		partner.setBusinessType("서비스업");
		partner.setBusinessItem("소프트웨어");
		partner.setPhone("PHONE" + token);
		partner.setMobile("MOBILE" + token);
		partner.setEmail("controller" + token + "@hexa.test");
		partner.setPostalCode("12345");
		partner.setAddress("컨트롤러 테스트 주소 " + token);
		partner.setNote("컨트롤러 테스트 적요 " + token);

		return partner;
	}

	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}
