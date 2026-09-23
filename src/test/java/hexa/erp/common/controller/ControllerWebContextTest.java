package hexa.erp.common.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hexa.erp.assignee.controller.AssigneeLookupController;
import hexa.erp.assignee.mapper.AssigneeMapper;
import hexa.erp.assignee.service.AssigneeService;
import hexa.erp.item.controller.ItemLookupController;
import hexa.erp.partner.controller.PartnerLookupController;
import hexa.erp.quotation.controller.QuotationLookupController;
import hexa.erp.sale.controller.SaleLookupController;
import hexa.erp.salesorder.controller.SalesOrderLookupController;
import hexa.erp.shipinstruction.controller.ShipInstructionLookupController;
import hexa.erp.warehouse.controller.WarehouseLookupController;
import hexa.erp.testinfra.ControllerWebAssertions;
import hexa.erp.testinfra.StubAssigneeService;

/** Stub Service로 웹 계층을 검사한다. JSP 렌더링·DB 연결은 포함하지 않는다. */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration("src/main/webapp")
@ContextHierarchy({
		@ContextConfiguration(name = "root", locations = "classpath:spring/web-test-context.xml"),
		@ContextConfiguration(name = "mvc",
				locations = "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml") })
public class ControllerWebContextTest {
	@Inject
	private WebApplicationContext context;
	private MockMvc mvc;

	@Before
	public void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void startsWithRequiredParentServiceAndNoDatabase() {
		assertNotNull(context.getParent());
		assertEquals(0, BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context, DataSource.class).length);
		assertEquals(1, BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context, AssigneeService.class).length);
		assertSame(context.getParent().getBean(AssigneeService.class), context.getBean(AssigneeService.class));
		assertTrue(context.getBean(AssigneeService.class) instanceof StubAssigneeService);
		assertEquals(0, BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context, AssigneeMapper.class).length);
		assertEquals(1, context.getBeansOfType(CommonViewAdvice.class).size());
		assertEquals(1, context.getBeansOfType(HomeController.class).size());
		assertEquals(1, context.getBeansOfType(AssigneeLookupController.class).size());
		ControllerWebAssertions.onlyExpectedApplicationComponents(context);
	}

	@Test
	public void homeKeepsContextRelativeRedirect() throws Exception {
		MvcResult root = mvc.perform(get("/")).andReturn();
		assertEquals(302, root.getResponse().getStatus());
		assertEquals("/master/partner", root.getResponse().getRedirectedUrl());
		MvcResult deployed = mvc.perform(get("/hexa-erp/").contextPath("/hexa-erp")).andReturn();
		assertEquals(302, deployed.getResponse().getStatus());
		assertEquals("/hexa-erp/master/partner", deployed.getResponse().getRedirectedUrl());
	}

	@Test
	public void allScreensHaveEmptyModels() throws Exception {
		ControllerWebAssertions.emptyViews(mvc);
	}

	@Test
	public void editRequestsNeverCreateExistingData() throws Exception {
		ControllerWebAssertions.editReadsAreExplicitlyUnimplemented(mvc);
	}

	@Test
	public void unimplementedLookupsRemainEmptyAndUnknownKindsAre404() throws Exception {
		ControllerWebAssertions.lookupContracts(mvc);
	}

	@Test
	public void unimplementedLookupsDoNotRequireAssigneeService() throws Exception {
		MockMvc independent = MockMvcBuilders.standaloneSetup(
				new PartnerLookupController(), new WarehouseLookupController(), new ItemLookupController(),
				new QuotationLookupController(), new SalesOrderLookupController(), new SaleLookupController(),
				new ShipInstructionLookupController()).build();
		ControllerWebAssertions.lookupContracts(independent);
		assertEquals(404, independent.perform(get("/lookup/options/assignee"))
				.andReturn().getResponse().getStatus());
	}

	@Test
	public void assigneeLookupUsesRequiredParentService() throws Exception {
		MvcResult result = mvc.perform(get("/hexa-erp/lookup/options/assignee")
				.contextPath("/hexa-erp").param("page", "99")).andReturn();
		assertEquals(200, result.getResponse().getStatus());
		assertTrue(result.getHandler() instanceof HandlerMethod);
		assertEquals(AssigneeLookupController.class, ((HandlerMethod) result.getHandler()).getBeanType());
		JsonNode body = new ObjectMapper().readTree(result.getResponse().getContentAsByteArray());
		assertEquals(5, body.size());
		assertEquals(1, body.get("page").asInt());
		assertEquals(1, body.get("totalPages").asInt());
		assertEquals(1, body.get("totalCount").asInt());
		assertEquals(25, body.get("pageSize").asInt());
		assertEquals(1, body.get("rows").size());
		JsonNode row = body.get("rows").get(0);
		assertTrue(row.get("assigneeId").isTextual());
		assertEquals("9007199254740993", row.get("assigneeId").asText());
		assertTrue(row.get("assigneeCode").isTextual());
		assertEquals("00001", row.get("assigneeCode").asText());
		assertEquals("웹 테스트 담당자", row.get("assigneeName").asText());
		assertEquals("Y", row.get("activeFlag").asText());
	}

	@Test
	public void directPostsNeverReportSuccess() throws Exception {
		ControllerWebAssertions.writesAreBlocked(mvc);
	}

	@Test
	public void scalarAndRepeatedConditionsSurviveEmptyResults() throws Exception {
		ControllerWebAssertions.searchConditions(mvc);
	}

	@Test
	public void everyExactUrlHasOneOwner() {
		ControllerWebAssertions.exactMappings(context.getBean(RequestMappingHandlerMapping.class));
	}
}
