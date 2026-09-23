package hexa.erp.assignee.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hexa.erp.assignee.domain.AssigneeVO;
import hexa.erp.assignee.service.AssigneeService;
import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.testinfra.ControllerWebAssertions;

/** 테스트 Service를 사용하며 실제 Oracle에는 연결하지 않는다. */
public class AssigneeLookupControllerTest {
	private AssigneeLookupController controller;
	private StubAssigneeService service;
	private MockMvc mvc;
	private final ObjectMapper json = new ObjectMapper();

	@Before
	public void setUp() {
		controller = new AssigneeLookupController();
		service = new StubAssigneeService();
		controller.setAssigneeService(service);
		mvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	public void missingRequiredServicePreventsContextStartup() {
		try (GenericApplicationContext context = new GenericApplicationContext()) {
			AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
			context.registerBeanDefinition("assigneeLookupController", new RootBeanDefinition(AssigneeLookupController.class));
			try {
				context.refresh();
				fail("AssigneeService 없이 Controller가 등록되면 안 된다.");
			} catch (UnsatisfiedDependencyException expected) {
				assertTrue(expected.getMessage().contains("AssigneeService"));
			}
		}
	}

	@Test
	public void trimsKeywordBindsPageAndIgnoresRequestedAmount() throws Exception {
		service.total = 26;
		service.rows = Collections.singletonList(row());
		JsonNode body = body(mvc.perform(get("/lookup/options/assignee")
				.param("keyword", "  김 담당자  ").param("page", "2")
				.param("amount", "1000").param("pageSize", "1000")).andReturn());
		assertEnvelope(body, 2, 2, 26);
		assertEquals(1, body.get("rows").size());
		assertEquals(2, service.calls.size());
		for (LookupCriteria criteria : service.calls) {
			assertEquals("김 담당자", criteria.getKeyword());
			assertEquals(2, criteria.getPageNum());
			assertEquals(25, criteria.getAmount());
		}
		JsonNode selected = body.get("rows").get(0);
		assertEquals(4, selected.size());
		assertTrue(selected.get("assigneeId").isTextual());
		assertEquals("999999999999999999", selected.get("assigneeId").asText());
		assertTrue(selected.get("assigneeCode").isTextual());
		assertEquals("00001", selected.get("assigneeCode").asText());
		assertEquals("김 담당자", selected.get("assigneeName").asText());
		assertEquals("Y", selected.get("activeFlag").asText());
		assertEquals(Long.valueOf(999999999999999999L), service.rows.get(0).getAssigneeId());
	}

	@Test
	public void missingOrNonPositivePageUsesFirstPage() throws Exception {
		service.total = 80;
		assertEnvelope(body(mvc.perform(get("/lookup/options/assignee")).andReturn()), 1, 4, 80);
		assertEquals("", service.calls.get(0).getKeyword());
		for (String page : new String[] { "", "0", "-7", "-2147483648" }) {
			service.calls.clear();
			assertEnvelope(body(mvc.perform(get("/lookup/options/assignee")
					.param("page", page).param("keyword", "   ")).andReturn()), 1, 4, 80);
			assertEquals(1, service.calls.get(0).getPageNum());
			assertEquals(1, service.calls.get(1).getPageNum());
		}
	}

	@Test
	public void outOfRangePageIsClampedBeforeListQueryAndInJson() throws Exception {
		service.total = 26;
		assertEnvelope(body(mvc.perform(get("/lookup/options/assignee")
				.param("page", "99")).andReturn()), 2, 2, 26);
		assertEquals(99, service.calls.get(0).getPageNum()); // count는 페이지를 사용하지 않는다.
		assertEquals(2, service.calls.get(1).getPageNum());
	}

	@Test
	public void emptyCountsAndIntegerBoundaryKeepSafeTotalPages() throws Exception {
		int[][] cases = { { 0, 1 }, { 1, 1 }, { 25, 1 }, { 26, 2 }, { 50, 2 }, { 51, 3 },
				{ Integer.MAX_VALUE, 85899346 } };
		for (int[] test : cases) {
			service.total = test[0];
			service.calls.clear();
			JsonNode body = body(mvc.perform(get("/lookup/options/assignee")
					.param("page", "2147483647")).andReturn());
			assertEnvelope(body, test[1], test[1], test[0]);
			assertEquals(test[1], service.calls.get(1).getPageNum());
			assertEquals(25, service.calls.get(1).getAmount());
		}
	}

	@Test
	public void invalidIntegerPagesAreRequestErrorsNotDatabaseQueries() throws Exception {
		for (String page : new String[] { "abc", "2147483648", "1.5" }) {
			assertEquals(400, mvc.perform(get("/lookup/options/assignee")
					.param("page", page)).andReturn().getResponse().getStatus());
		}
		assertTrue(service.calls.isEmpty());
	}

	@Test
	public void countAndListFailuresAreNeverReportedAsEmptySuccess() {
		RuntimeException failure = new IllegalStateException("테스트용 SQL 오류");
		service.countFailure = failure;
		try {
			controller.assigneeOptions("", 1);
			fail("DB 오류를 빈 성공 응답으로 바꾸면 안 된다.");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
		service.countFailure = null;
		service.listFailure = failure;
		try {
			controller.assigneeOptions("", 1);
			fail("목록 조회 오류도 숨기면 안 된다.");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
	}

	@Test
	public void actualWebXmlInjectsParentServiceWithoutReplacingController() throws Exception {
		try (GenericApplicationContext parent = new GenericApplicationContext();
				XmlWebApplicationContext web = new XmlWebApplicationContext()) {
			parent.getBeanFactory().registerSingleton("assigneeService", service);
			parent.refresh();
			web.setParent(parent);
			web.setAllowBeanDefinitionOverriding(false);
			web.setServletContext(new MockServletContext("src/main/webapp", new FileSystemResourceLoader()));
			web.setConfigLocation("file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml");
			web.refresh();
			service.total = 1;
			service.rows = Collections.singletonList(row());
			MockMvc actual = MockMvcBuilders.webAppContextSetup(web).build();
			JsonNode body = body(actual.perform(get("/hexa-erp/lookup/options/assignee")
					.contextPath("/hexa-erp")).andReturn());
			assertEnvelope(body, 1, 1, 1);
			assertEquals("00001", body.get("rows").get(0).get("assigneeCode").asText());
			assertEquals(1, web.getBeansOfType(AssigneeLookupController.class).size());
			assertEquals(0, BeanFactoryUtils.beanNamesForTypeIncludingAncestors(web, DataSource.class).length);
			ControllerWebAssertions.onlyExpectedApplicationComponents(web);
			ControllerWebAssertions.emptyViews(actual);
			ControllerWebAssertions.writesAreBlocked(actual);
			ControllerWebAssertions.exactMappings(web.getBean(RequestMappingHandlerMapping.class));
		}
	}

	private JsonNode body(MvcResult result) throws Exception {
		assertEquals(200, result.getResponse().getStatus());
		return json.readTree(result.getResponse().getContentAsByteArray());
	}

	private void assertEnvelope(JsonNode body, int page, int totalPages, int totalCount) {
		assertEquals(5, body.size());
		assertTrue(body.get("rows").isArray());
		assertEquals(page, body.get("page").asInt());
		assertEquals(totalPages, body.get("totalPages").asInt());
		assertEquals(totalCount, body.get("totalCount").asInt());
		assertEquals(25, body.get("pageSize").asInt());
	}

	private AssigneeVO row() {
		AssigneeVO row = new AssigneeVO();
		row.setAssigneeId(999999999999999999L);
		row.setAssigneeCode("00001");
		row.setAssigneeName("김 담당자");
		row.setActiveFlag("Y");
		return row;
	}

	private static class StubAssigneeService implements AssigneeService {
		private int total;
		private List<AssigneeVO> rows = Collections.emptyList();
		private final List<LookupCriteria> calls = new ArrayList<>();
		private RuntimeException countFailure;
		private RuntimeException listFailure;

		@Override
		public int getTotal(LookupCriteria criteria) {
			record(criteria);
			if (countFailure != null) throw countFailure;
			return total;
		}

		@Override
		public List<AssigneeVO> getList(LookupCriteria criteria) {
			record(criteria);
			if (listFailure != null) throw listFailure;
			return rows;
		}

		private void record(LookupCriteria criteria) {
			LookupCriteria copy = new LookupCriteria();
			copy.setPageNum(criteria.getPageNum());
			copy.setAmount(criteria.getAmount());
			copy.setKeyword(criteria.getKeyword());
			calls.add(copy);
		}
	}
}
