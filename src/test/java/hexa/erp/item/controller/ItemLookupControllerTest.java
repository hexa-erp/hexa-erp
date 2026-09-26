package hexa.erp.item.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.math.BigDecimal;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hexa.erp.item.domain.ItemLookupVO;
import hexa.erp.item.service.ItemLookupService;
import hexa.erp.common.domain.LookupCriteria;

/** 테스트 Service를 사용하며 실제 Oracle에는 연결하지 않는다. */
public class ItemLookupControllerTest {
	private ItemLookupController controller;
	private StubItemLookupService service;
	private MockMvc mvc;
	private final ObjectMapper json = new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

	@Before
	public void setUp() {
		controller = new ItemLookupController();
		service = new StubItemLookupService();
		controller.setItemLookupService(service);
		mvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	public void missingRequiredServicePreventsContextStartup() {
		try (GenericApplicationContext context = new GenericApplicationContext()) {
			AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
			context.registerBeanDefinition("itemLookupController", new RootBeanDefinition(ItemLookupController.class));
			try {
				context.refresh();
				fail("ItemLookupService 없이 Controller가 등록되면 안 된다.");
			} catch (UnsatisfiedDependencyException expected) {
				assertTrue(expected.getMessage().contains("ItemLookupService"));
			}
		}
	}

	@Test
	public void trimsKeywordBindsPageAndIgnoresRequestedAmount() throws Exception {
		service.total = 26;
		service.rows = Collections.singletonList(row());
		JsonNode body = body(mvc.perform(get("/lookup/options/item").param("keyword", "  김 품목  ").param("page", "2")
				.param("warehouseId", "201").param("amount", "1000").param("pageSize", "1000")).andReturn());
		assertEnvelope(body, 2, 2, 26);
		assertEquals(1, body.get("rows").size());
		assertEquals(2, service.calls.size());
		for (LookupCriteria criteria : service.calls) {
			assertEquals("김 품목", criteria.getKeyword());
			assertEquals(2, criteria.getPageNum());
			assertEquals(25, criteria.getAmount());
		}
		JsonNode selected = body.get("rows").get(0);
		assertEquals(8, selected.size());
		assertEquals(Collections.singletonList(201L), service.warehouseIds);
		assertTrue(selected.get("itemId").isIntegralNumber());
		assertEquals(42L, selected.get("itemId").asLong());
		assertTrue(selected.get("itemCode").isTextual());
		assertEquals("00001", selected.get("itemCode").asText());
		assertEquals("김 품목", selected.get("itemName").asText());
		assertEquals("선택 규격", selected.get("specification").asText());
		assertEquals("EA", selected.get("unit").asText());
		assertTrue(selected.get("outboundPrice").isNumber());
		assertEquals(0, new BigDecimal("9999999999999999.99").compareTo(selected.get("outboundPrice").decimalValue()));
		assertTrue(selected.get("stockQuantity").isNumber());
		assertEquals(0, new BigDecimal("12.375").compareTo(selected.get("stockQuantity").decimalValue()));
		assertEquals("Y", selected.get("activeFlag").asText());
		assertEquals(Long.valueOf(42L), service.rows.get(0).getItemId());
	}

	@Test
	public void omittedOrEmptyWarehouseKeepsItemsWithNullStock() throws Exception {
		service.total = 1;
		ItemLookupVO item = row();
		item.setStockQuantity(null);
		service.rows = Collections.singletonList(item);
		for (boolean emptyParameter : new boolean[] { false, true }) {
			MockHttpServletRequestBuilder request = get("/lookup/options/item");
			if (emptyParameter)
				request.param("warehouseId", "");
			JsonNode result = body(mvc.perform(request).andReturn());
			assertEnvelope(result, 1, 1, 1);
			assertEquals(1, result.get("rows").size());
			assertTrue(result.get("rows").get(0).get("stockQuantity").isNull());
			assertNull(service.warehouseIds.get(service.warehouseIds.size() - 1));
		}
	}

	@Test
	public void selectedWarehouseAndZeroOrNegativeStockArePreserved() throws Exception {
		service.total = 1;
		for (String quantity : new String[] { "0", "-2.375", "999999999999999.999" }) {
			ItemLookupVO item = row();
			item.setStockQuantity(new BigDecimal(quantity));
			service.rows = Collections.singletonList(item);
			JsonNode result = body(mvc.perform(get("/lookup/options/item").param("warehouseId", "00202")).andReturn());
			assertEquals(Long.valueOf(202L), service.warehouseIds.get(service.warehouseIds.size() - 1));
			assertEquals(1, result.get("rows").size());
			JsonNode selected = result.get("rows").get(0);
			assertEquals(42L, selected.get("itemId").asLong());
			assertEquals(0, new BigDecimal(quantity).compareTo(selected.get("stockQuantity").decimalValue()));
		}
	}

	@Test
	public void warehouseIdUsesLongAndInvalidValuesDoNotReachService() throws Exception {
		service.total = 1;
		body(mvc.perform(get("/lookup/options/item").param("warehouseId", "9007199254740993")).andReturn());
		assertEquals(Collections.singletonList(9007199254740993L), service.warehouseIds);
		service.calls.clear();
		service.warehouseIds.clear();
		for (String value : new String[] { "WARE-001", "1.5", "9223372036854775808" }) {
			assertEquals(400, mvc.perform(get("/lookup/options/item").param("warehouseId", value)).andReturn()
					.getResponse().getStatus());
		}
		assertTrue(service.calls.isEmpty());
		assertTrue(service.warehouseIds.isEmpty());
	}

	@Test
	public void missingOrNonPositivePageUsesFirstPage() throws Exception {
		service.total = 80;
		assertEnvelope(body(mvc.perform(get("/lookup/options/item")).andReturn()), 1, 4, 80);
		assertEquals("", service.calls.get(0).getKeyword());
		for (String page : new String[] { "", "0", "-7", "-2147483648" }) {
			service.calls.clear();
			assertEnvelope(body(
					mvc.perform(get("/lookup/options/item").param("page", page).param("keyword", "   ")).andReturn()),
					1, 4, 80);
			assertEquals(1, service.calls.get(0).getPageNum());
			assertEquals(1, service.calls.get(1).getPageNum());
		}
	}

	@Test
	public void outOfRangePageIsClampedBeforeListQueryAndInJson() throws Exception {
		service.total = 26;
		assertEnvelope(body(
				mvc.perform(get("/lookup/options/item").param("page", "99").param("warehouseId", "201")).andReturn()),
				2, 2, 26);
		assertEquals(Collections.singletonList(201L), service.warehouseIds);
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
			JsonNode body = body(mvc.perform(get("/lookup/options/item").param("page", "2147483647")).andReturn());
			assertEnvelope(body, test[1], test[1], test[0]);
			assertEquals(test[1], service.calls.get(1).getPageNum());
			assertEquals(25, service.calls.get(1).getAmount());
		}
	}

	@Test
	public void invalidIntegerPagesAreRequestErrorsNotDatabaseQueries() throws Exception {
		for (String page : new String[] { "abc", "2147483648", "1.5" }) {
			assertEquals(400,
					mvc.perform(get("/lookup/options/item").param("page", page)).andReturn().getResponse().getStatus());
		}
		assertTrue(service.calls.isEmpty());
	}

	@Test
	public void countAndListFailuresAreNeverReportedAsEmptySuccess() {
		RuntimeException failure = new IllegalStateException("테스트용 SQL 오류");
		service.countFailure = failure;
		try {
			controller.itemOptions("", 1, 201L);
			fail("DB 오류를 빈 성공 응답으로 바꾸면 안 된다.");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
		service.countFailure = null;
		service.listFailure = failure;
		try {
			controller.itemOptions("", 1, 201L);
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

	private ItemLookupVO row() {
		ItemLookupVO row = new ItemLookupVO();
		row.setItemId(42L);
		row.setItemCode("00001");
		row.setItemName("김 품목");
		row.setSpecification("선택 규격");
		row.setUnit("EA");
		row.setOutboundPrice(new BigDecimal("9999999999999999.99"));
		row.setStockQuantity(new BigDecimal("12.375"));
		row.setActiveFlag("Y");
		return row;
	}

	private static class StubItemLookupService implements ItemLookupService {
		@Override
		public ItemLookupVO get(Long id) {
			for (ItemLookupVO row : rows) {
				if (id.equals(row.getItemId()))
					return row;
			}
			return null;
		}

		private int total;
		private List<ItemLookupVO> rows = Collections.emptyList();
		private final List<LookupCriteria> calls = new ArrayList<>();
		private final List<Long> warehouseIds = new ArrayList<>();
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
		public List<ItemLookupVO> getList(LookupCriteria criteria, Long warehouseId) {
			record(criteria);
			warehouseIds.add(warehouseId);
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
