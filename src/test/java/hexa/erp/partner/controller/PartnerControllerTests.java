package hexa.erp.partner.controller;

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

import hexa.erp.partner.domain.PartnerCriteria;
import hexa.erp.partner.domain.PartnerVO;
import hexa.erp.partner.mapper.PartnerMapper;
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
public class PartnerControllerTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PartnerMapper mapper;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .build();
    }

    // 거래처 목록 요청과 Model 데이터 확인
    @Test
    public void testList() throws Exception {
        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/master/partner")
                    .param("page", "1")
                    .param("pageSize", "50")
                    .param("keyword", "")
                    .param("includeInactive", "N")
            )
            .andReturn();

        String viewName = result.getModelAndView().getViewName();
        Object partnerList = result.getModelAndView()
            .getModelMap()
            .get("partnerList");

        log.info("========== CONTROLLER LIST TEST ==========");
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info("VIEW NAME: " + viewName);
        log.info("MODEL MAP: "
            + result.getModelAndView().getModelMap());
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

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/master/partner")
                    .param("page", "1")
                    .param("pageSize", "50")
                    .param("keyword", partner.getRepresentative())
                    .param("includeInactive", "Y")
            )
            .andReturn();

        List<PartnerVO> partnerList = getPartnerList(result);
        PartnerVO searchedPartner = findById(
            partnerList,
            partner.getPartnerId()
        );

        log.info("========== CONTROLLER SEARCH TEST ==========");
        log.info("SEARCH KEYWORD: " + partner.getRepresentative());
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info("LIST SIZE: " + partnerList.size());
        log.info("SEARCHED PARTNER: " + searchedPartner);

        assertEquals(200, result.getResponse().getStatus());
        assertNotNull(searchedPartner);
    }

    // 거래처 등록 요청과 DB 저장 결과 확인
    @Test
    public void testRegister() throws Exception {
        PartnerVO partner = createPartner();

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/partner/save")
                    .param("partnerCode", partner.getPartnerCode())
                    .param("partnerName", partner.getPartnerName())
                    .param("businessNo", partner.getBusinessNo())
                    .param("representative", partner.getRepresentative())
                    .param("businessType", partner.getBusinessType())
                    .param("businessItem", partner.getBusinessItem())
                    .param("phone", partner.getPhone())
                    .param("mobile", partner.getMobile())
                    .param("email", partner.getEmail())
                    .param("postalCode", partner.getPostalCode())
                    .param("address", partner.getAddress())
                    .param("note", partner.getNote())
            )
            .andReturn();

        String viewName = result.getModelAndView().getViewName();

        PartnerCriteria criteria = createCriteria(partner.getPartnerCode());
        List<PartnerVO> savedList = mapper.getListWithPaging(criteria);

        log.info("========== CONTROLLER REGISTER TEST ==========");
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info("VIEW NAME: " + viewName);
        log.info("PARTNER CODE: " + partner.getPartnerCode());
        log.info("SAVED LIST SIZE: " + savedList.size());
        savedList.forEach(row -> log.info("SAVED PARTNER: " + row));

        assertEquals(302, result.getResponse().getStatus());
        assertTrue(viewName.startsWith("redirect:/master/partner"));
        assertFalse(savedList.isEmpty());
    }

    // 거래처 수정 요청과 DB 변경 결과 확인
    @Test
    public void testModify() throws Exception {
        PartnerVO partner = createPartner();
        mapper.insertSelectKey(partner);

        String changedName = "컨트롤러 수정 거래처 " + createToken();

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/partner/save")
                    .param("partnerId", String.valueOf(partner.getPartnerId()))
                    .param("partnerCode", partner.getPartnerCode())
                    .param("partnerName", changedName)
                    .param("businessNo", partner.getBusinessNo())
                    .param("representative", partner.getRepresentative())
                    .param("businessType", partner.getBusinessType())
                    .param("businessItem", partner.getBusinessItem())
                    .param("phone", "02-9999-9999")
                    .param("mobile", partner.getMobile())
                    .param("email", partner.getEmail())
                    .param("postalCode", partner.getPostalCode())
                    .param("address", partner.getAddress())
                    .param("note", partner.getNote())
            )
            .andReturn();

        PartnerCriteria criteria = createCriteria(changedName);

        PartnerVO changedPartner = findById(
            mapper.getListWithPaging(criteria),
            partner.getPartnerId()
        );

        log.info("========== CONTROLLER MODIFY TEST ==========");
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info("VIEW NAME: "
            + result.getModelAndView().getViewName());
        log.info("PARTNER ID: " + partner.getPartnerId());
        log.info("CHANGED PARTNER: " + changedPartner);

        assertEquals(302, result.getResponse().getStatus());
        assertNotNull(changedPartner);
        assertEquals(changedName, changedPartner.getPartnerName());
        assertEquals("02-9999-9999", changedPartner.getPhone());
    }

    // 거래처 사용중단 요청과 상태 변경 결과 확인
    @Test
    public void testChangeActive() throws Exception {
        PartnerVO partner = createPartner();
        mapper.insertSelectKey(partner);

        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/partner/active")
                    .param("ids", String.valueOf(partner.getPartnerId()))
                    .param("activeFlag", "N")
            )
            .andReturn();

        PartnerCriteria criteria = createCriteria(partner.getPartnerCode());

        PartnerVO changedPartner = findById(
            mapper.getListWithPaging(criteria),
            partner.getPartnerId()
        );

        log.info("========== CONTROLLER CHANGE ACTIVE TEST ==========");
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info("VIEW NAME: "
            + result.getModelAndView().getViewName());
        log.info("PARTNER ID: " + partner.getPartnerId());
        log.info("ACTIVE FLAG: " + changedPartner.getActiveFlag());

        assertEquals(302, result.getResponse().getStatus());
        assertNotNull(changedPartner);
        assertEquals("N", changedPartner.getActiveFlag());
    }

    // 필수값 누락 안내 메시지 확인
    @Test
    public void testSaveValidation() throws Exception {
        MvcResult result = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/master/partner/save")
                    .param("partnerCode", "")
                    .param("partnerName", "")
            )
            .andReturn();

        String viewName = result.getModelAndView().getViewName();
        Object errorMessage = result.getFlashMap().get("errorMessage");

        log.info("========== CONTROLLER VALIDATION TEST ==========");
        log.info("HTTP STATUS: " + result.getResponse().getStatus());
        log.info("VIEW NAME: " + viewName);
        log.info("ERROR MESSAGE: " + errorMessage);

        assertEquals(302, result.getResponse().getStatus());
        assertTrue(viewName.startsWith("redirect:/master/partner"));
        assertEquals(
            "거래처 코드와 상호를 입력해 주세요.",
            errorMessage
        );
    }

    @SuppressWarnings("unchecked")
    private List<PartnerVO> getPartnerList(MvcResult result) {
        return (List<PartnerVO>) result
            .getModelAndView()
            .getModelMap()
            .get("partnerList");
    }

    private PartnerCriteria createCriteria(String keyword) {
        PartnerCriteria criteria = new PartnerCriteria();

        criteria.setPage(1);
        criteria.setPageSize(50);
        criteria.setKeyword(keyword);
        criteria.setIncludeInactive("Y");

        return criteria;
    }

    private PartnerVO findById(List<PartnerVO> list, Long partnerId) {
        return list.stream()
            .filter(partner -> partnerId.equals(partner.getPartnerId()))
            .findFirst()
            .orElse(null);
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
        return UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12);
    }
}
