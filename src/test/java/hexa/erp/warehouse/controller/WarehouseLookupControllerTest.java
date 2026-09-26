package hexa.erp.warehouse.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.warehouse.service.WarehouseLookupService;
import hexa.erp.common.domain.LookupCriteria;

/** 테스트 Service를 사용하며 실제 Oracle에는 연결하지 않는다. */
public class WarehouseLookupControllerTest {
	private WarehouseLookupController controller;
	private StubWarehouseLookupService service;
	private MockMvc mvc;
	private final ObjectMapper json = new ObjectMapper();

	@Before
	public void setUp() {
		controller = new WarehouseLookupController();
		service = new StubWarehouseLookupService();
		controller.setWarehouseLookupService(service);
		mvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	public void missingRequiredServicePreventsContextStartup() {
		try (GenericApplicationContext context = new GenericApplicationContext()) {
			AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
			context.registerBeanDefinition("warehouseLookupController",
					new RootBeanDefinition(WarehouseLookupController.class));
			try {
				context.refresh();
				fail("WarehouseLookupService 없이 Controller가 등록되면 안 된다.");
			} catch (UnsatisfiedDependencyException expected) {
				assertTrue(expected.getMessage().contains("WarehouseLookupService"));
			}
		}
	}

	@Test
	public void trimsKeywordBindsPageAndIgnoresRequestedAmount() throws Exception {
		service.total = 26;
		service.rows = Collections.singletonList(row());
		JsonNode body = body(mvc.perform(get("/lookup/options/warehouse").param("keyword", "  김 창고  ")
				.param("page", "2").param("amount", "1000").param("pageSize", "1000")).andReturn());
		assertEnvelope(body, 2, 2, 26);
		assertEquals(1, body.get("rows").size());
		assertEquals(2, service.calls.size());
		for (LookupCriteria criteria : service.calls) {
			assertEquals("김 창고", criteria.getKeyword());
			assertEquals(2, criteria.getPageNum());
			assertEquals(25, criteria.getAmount());
		}
		JsonNode selected = body.get("rows").get(0);
		assertEquals(5, selected.size());
		assertTrue(selected.get("warehouseId").isIntegralNumber());
		assertEquals(42L, selected.get("warehouseId").asLong());
		assertTrue(selected.get("warehouseCode").isTextual());
		assertEquals("00001", selected.get("warehouseCode").asText());
		assertEquals("김 창고", selected.get("warehouseName").asText());
		assertEquals("창고", selected.get("warehouseType").asText());
		assertEquals("Y", selected.get("activeFlag").asText());
		assertEquals(Long.valueOf(42L), service.rows.get(0).getWarehouseId());
	}

	@Test
	public void missingOrNonPositivePageUsesFirstPage() throws Exception {
		service.total = 80;
		assertEnvelope(body(mvc.perform(get("/lookup/options/warehouse")).andReturn()), 1, 4, 80);
		assertEquals("", service.calls.get(0).getKeyword());
		for (String page : new String[] { "", "0", "-7", "-2147483648" }) {
			service.calls.clear();
			assertEnvelope(body(mvc
					.perform(get("/lookup/options/warehouse").param("page", page).param("keyword", "   ")).andReturn()),
					1, 4, 80);
			assertEquals(1, service.calls.get(0).getPageNum());
			assertEquals(1, service.calls.get(1).getPageNum());
		}
	}

	@Test
	public void outOfRangePageIsClampedBeforeListQueryAndInJson() throws Exception {
		service.total = 26;
		assertEnvelope(body(mvc.perform(get("/lookup/options/warehouse").param("page", "99")).andReturn()), 2, 2, 26);
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
			JsonNode body = body(mvc.perform(get("/lookup/options/warehouse").param("page", "2147483647")).andReturn());
			assertEnvelope(body, test[1], test[1], test[0]);
			assertEquals(test[1], service.calls.get(1).getPageNum());
			assertEquals(25, service.calls.get(1).getAmount());
		}
	}

	@Test
	public void invalidIntegerPagesAreRequestErrorsNotDatabaseQueries() throws Exception {
		for (String page : new String[] { "abc", "2147483648", "1.5" }) {
			assertEquals(400, mvc.perform(get("/lookup/options/warehouse").param("page", page)).andReturn()
					.getResponse().getStatus());
		}
		assertTrue(service.calls.isEmpty());
	}

	@Test
	public void countAndListFailuresAreNeverReportedAsEmptySuccess() {
		RuntimeException failure = new IllegalStateException("테스트용 SQL 오류");
		service.countFailure = failure;
		try {
			controller.warehouseOptions("", 1);
			fail("DB 오류를 빈 성공 응답으로 바꾸면 안 된다.");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
		service.countFailure = null;
		service.listFailure = failure;
		try {
			controller.warehouseOptions("", 1);
			fail("목록 조회 오류도 숨기면 안 된다.");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
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

	private WarehouseLookupVO row() {
		WarehouseLookupVO row = new WarehouseLookupVO();
		row.setWarehouseId(42L);
		row.setWarehouseCode("00001");
		row.setWarehouseName("김 창고");
		row.setWarehouseType("창고");
		row.setActiveFlag("Y");
		return row;
	}

	private static class StubWarehouseLookupService implements WarehouseLookupService {
		@Override
		public WarehouseLookupVO get(Long id) {
			for (WarehouseLookupVO row : rows) {
				if (id.equals(row.getWarehouseId()))
					return row;
			}
			return null;
		}

		private int total;
		private List<WarehouseLookupVO> rows = Collections.emptyList();
		private final List<LookupCriteria> calls = new ArrayList<>();
		private RuntimeException countFailure;
		private RuntimeException listFailure;

		@Override
		public int getTotal(LookupCriteria criteria) {
			record(criteria);
			if (countFailure != null)
				throw countFailure;
			return total;
		}

		@Override
		public List<WarehouseLookupVO> getList(LookupCriteria criteria) {
			record(criteria);
			if (listFailure != null)
				throw listFailure;
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
