package hexa.erp.testinfra;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hexa.erp.common.domain.FilterSelectionVO;

/** Stub 부모를 사용하는 웹 테스트의 기대 계약. JSP를 렌더링하거나 DB에 접속하지 않는다. */
public final class ControllerWebAssertions {
	private ControllerWebAssertions() {
	}

	private static final String[][] VIEWS = { { "/master/partner", "master/partner", "partnerList" },
			{ "/master/warehouse", "master/warehouse", "warehouseList" }, { "/master/item", "master/item", "itemList" },
			{ "/quotation/list", "quotation/list", "quotationList" }, { "/quotation/form", "quotation/form", "form" },
			{ "/quotation/status", "quotation/status", "monthGroups" },
			{ "/quotation/statement", "quotation/statement", "form" }, { "/order/list", "order/list", "orderList" },
			{ "/order/form", "order/form", "form" }, { "/order/status", "order/status", "monthGroups" },
			{ "/order/statement", "order/statement", "form" }, { "/sale/list", "sale/list", "saleList" },
			{ "/sale/form", "sale/form", "form" }, { "/sale/status", "sale/status", "monthGroups" },
			{ "/sale/statement", "sale/statement", "form" },
			{ "/shipping-instruction/list", "shippingInstruction/list", "shipInstructionList" },
			{ "/shipping-instruction/form", "shippingInstruction/form", "form" },
			{ "/shipment/list", "shipment/list", "shipmentList" }, { "/shipment/form", "shipment/form", "form" },
			{ "/quotation/unordered", "quotation/unordered", "monthGroups" },
			{ "/order/unsold", "order/unsold", "monthGroups" },
			{ "/sale/bulk-price", "sale/bulk-price", "salePriceList" } };
	private static final String[][] LOOKUPS = {
			{ "/lookup/options/partner", "hexa.erp.partner.controller.PartnerLookupController" },
			{ "/lookup/options/warehouse", "hexa.erp.warehouse.controller.WarehouseLookupController" },
			{ "/lookup/options/item", "hexa.erp.item.controller.ItemLookupController" },
			{ "/lookup/options/assignee", "hexa.erp.assignee.controller.AssigneeLookupController" },
			{ "/lookup/sources/quotation", "hexa.erp.quotation.controller.QuotationLookupController" },
			{ "/lookup/sources/order", "hexa.erp.salesorder.controller.SalesOrderLookupController" },
			{ "/lookup/sources/sale", "hexa.erp.sale.controller.SaleLookupController" },
			{ "/lookup/sources/shipping-instruction",
					"hexa.erp.shipinstruction.controller.ShipInstructionLookupController" } };
	private static final String[] WRITES = { "/master/partner/save", "/master/partner/active", "/master/warehouse/save",
			"/master/warehouse/active", "/master/item/save", "/master/item/active", "/master/item/stock",
			"/quotation/save", "/quotation/delete", "/quotation/change-status", "/order/save", "/order/delete",
			"/order/change-status", "/sale/save", "/sale/delete", "/sale/change-status", "/shipping-instruction/save",
			"/shipping-instruction/delete", "/shipping-instruction/change-status", "/shipment/save", "/shipment/delete",
			"/shipment/change-status", "/sale/bulk-price/save" };

	public static void viewContracts(MockMvc mvc) throws Exception {
		for (String[] route : VIEWS) {
			MvcResult result = mvc.perform(get(route[0]).servletPath(route[0])).andReturn();
			assertEquals(route[0], 200, result.getResponse().getStatus());
			ModelAndView mv = result.getModelAndView();
			assertNotNull(route[0], mv);
			assertEquals(route[1], mv.getViewName());
			Map<String, Object> model = mv.getModel();
			assertEquals(route[0], model.get("currentPath"));
			assertTrue(model.get("developmentMode") instanceof Boolean);
			assertNotNull(model.get("navigation"));
			assertNotNull(model.get("connectedAt"));
			assertNotNull(model.get("pageTitle"));
			assertNotNull(route[2], model.get(route[2]));
			if ("form".equals(route[2])) {
				Object form = model.get("form");
				Object lines = form instanceof Map<?, ?> ? ((Map<?, ?>) form).get("lines")
						: PropertyAccessorFactory.forBeanPropertyAccess(form).getPropertyValue("lines");
				assertTrue("form.lines", lines instanceof List<?>);
				if (route[0].endsWith("/form")) {
					assertEquals("create", model.get("mode"));
					assertEquals(Boolean.FALSE, model.get("isEdit"));
				} else {
					assertTrue(model.get("statementTotals") instanceof Map<?, ?>);
				}
			} else {
				assertTrue(model.get(route[2]) instanceof List<?>);
				assertNotNull(model.get("search"));
				boolean report = route[0].endsWith("/status") || route[0].endsWith("/unordered")
						|| route[0].endsWith("/unsold");
				if (report) {
					assertTrue(model.get("monthGroups") instanceof List<?>);
					if (route[0].endsWith("/status")) {
						assertTrue(model.get("statusTotals") instanceof Map<?, ?>);
					} else {
						assertTrue(model.get("remainingTotals") instanceof Map<?, ?>);
					}
					assertFalse(model.containsKey("page"));
					assertFalse(model.containsKey("pageSize"));
				} else {
					for (String field : Arrays.asList("page", "totalPages", "totalCount", "pageSize")) {
						assertTrue(field, model.get(field) instanceof Number);
					}
					paging(((Number) model.get("page")).longValue(), ((Number) model.get("totalPages")).longValue(),
							((Number) model.get("totalCount")).longValue(),
							((Number) model.get("pageSize")).longValue(), ((List<?>) model.get(route[2])).size());
					assertEquals(route[0], model.get("basePath"));
				}
			}
		}
	}

	public static void sourceLookupContracts(MockMvc mvc) throws Exception {
		ObjectMapper json = new ObjectMapper();
		for (String[] route : LOOKUPS) {
			String url = route[0];
			if (url.startsWith("/lookup/options/")) {
				continue;
			}
			MvcResult result = mvc
					.perform(get(url).param("keyword", "001").param("page", "2").param("progressStatus", "IN_PROGRESS"))
					.andReturn();
			assertEquals(url, 200, result.getResponse().getStatus());
			assertTrue(result.getHandler() instanceof HandlerMethod);
			assertEquals(url, route[1], ((HandlerMethod) result.getHandler()).getBeanType().getName());
			JsonNode body = json.readTree(result.getResponse().getContentAsString());
			lookupResponse(body);
		}
		assertEquals(404, mvc.perform(get("/lookup/options/unknown")).andReturn().getResponse().getStatus());
		assertEquals(404, mvc.perform(get("/lookup/sources/unknown")).andReturn().getResponse().getStatus());
		assertEquals(404, mvc.perform(get("/unknown/list")).andReturn().getResponse().getStatus());
	}

	public static void lookupResponse(JsonNode body) {
		assertNotNull(body);
		assertTrue(body.isObject());
		assertTrue(body.path("rows").isArray());
		for (String field : Arrays.asList("page", "totalPages", "totalCount", "pageSize")) {
			assertTrue(field, body.path(field).isIntegralNumber());
		}
		paging(body.get("page").asLong(), body.get("totalPages").asLong(), body.get("totalCount").asLong(),
				body.get("pageSize").asLong(), body.get("rows").size());
	}

	private static void paging(long page, long totalPages, long totalCount, long pageSize, int rowCount) {
		assertTrue("page", page >= 1);
		assertTrue("totalPages", totalPages >= 1);
		assertTrue("totalCount", totalCount >= 0);
		assertTrue("pageSize", pageSize > 0);
		assertTrue("page <= totalPages", page <= totalPages);
		assertEquals("totalPages", Math.max(1L, totalCount / pageSize + (totalCount % pageSize == 0 ? 0 : 1)),
				totalPages);
		assertTrue("rows <= pageSize", rowCount <= pageSize);
		assertTrue("rows <= totalCount", rowCount <= totalCount);
	}

	public static void searchConditions(MockMvc mvc) throws Exception {
		String[] urls = { "/quotation/status", "/quotation/unordered", "/order/status", "/order/unsold", "/sale/status",
				"/sale/bulk-price" };
		for (String url : urls) {
			for (String view : Arrays.asList("search", "results")) {
				MvcResult result = mvc.perform(get(url).param("view", view).param("keyword", "보존 확인")
						.param("datePreset", "custom").param("startDate", "2026-01-01").param("endDate", "2026-02-28")
						.param("progressStatus", "IN_PROGRESS").param("page", "7").param("pageSize", "999")
						.param("warehouseIds", "201", "202").param("partnerIds", "101", "102")
						.param("itemIds", "301", "302").param("assigneeIds", "401", "402")).andReturn();
				assertEquals(200, result.getResponse().getStatus());
				Map<String, Object> model = result.getModelAndView().getModel();
				Map<?, ?> search = (Map<?, ?>) model.get("search");
				assertEquals("보존 확인", search.get("keyword"));
				assertEquals("IN_PROGRESS", search.get("progressStatus"));
				assertEquals("2026-01-01", search.get("startDate"));
				assertEquals("2026-02-28", search.get("endDate"));
				assertEquals(view, search.get("view"));
				Map<?, ?> ids = (Map<?, ?>) model.get("filterIds");
				assertEquals(Arrays.asList("201", "202"), ids.get("warehouseIds"));
				assertEquals(Arrays.asList("101", "102"), ids.get("partnerIds"));
				assertEquals(Arrays.asList("301", "302"), ids.get("itemIds"));
				Map<?, ?> selections = (Map<?, ?>) model.get("filterSelections");
				assertSelections(selections, "warehouse", "웹 테스트 창고 ", "201", "202");
				assertSelections(selections, "partner", "웹 테스트 거래처 ", "101", "102");
				assertSelections(selections, "item", "웹 테스트 품목 ", "301", "302");
				assertFalse(search.containsKey("partnerIds"));
				if (url.endsWith("bulk-price")) {
					assertFalse(ids.containsKey("assigneeIds"));
					assertFalse(selections.containsKey("assignee"));
					assertFalse(search.containsKey("assigneeIds"));
				} else {
					assertEquals(Arrays.asList("401", "402"), ids.get("assigneeIds"));
					assertSelections(selections, "assignee", "웹 테스트 담당자 ", "401", "402");
					assertFalse(search.containsKey("page"));
					assertFalse(search.containsKey("pageSize"));
				}
			}
		}
	}

	private static void assertSelections(Map<?, ?> selections, String kind, String namePrefix, String... ids) {
		List<?> rows = (List<?>) selections.get(kind);
		assertEquals(kind, ids.length, rows.size());
		for (int i = 0; i < ids.length; i++) {
			FilterSelectionVO row = (FilterSelectionVO) rows.get(i);
			assertEquals(ids[i], row.getId());
			assertEquals("00001", row.getCode());
			assertEquals(namePrefix + ids[i], row.getName());
		}
	}

	public static void requiredMappings(RequestMappingHandlerMapping mapping) {
		Map<String, String> expected = new LinkedHashMap<>();
		expected.put("GET /", "hexa.erp.common.controller.HomeController");
		for (String[] route : VIEWS) {
			expected.put("GET " + route[0], owner(route[0]));
		}
		for (String path : WRITES) {
			expected.put("POST " + path, owner(path));
		}
		for (String[] route : LOOKUPS) {
			expected.put("GET " + route[0], route[1]);
		}
		Set<String> found = new HashSet<>();
		for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mapping.getHandlerMethods().entrySet()) {
			for (String path : entry.getKey().getPatternsCondition().getPatterns()) {
				for (RequestMethod method : entry.getKey().getMethodsCondition().getMethods()) {
					String key = method.name() + " " + path;
					if (expected.containsKey(key)) {
						assertEquals(key, expected.get(key), entry.getValue().getBeanType().getName());
						found.add(key);
					}
				}
			}
		}
		assertEquals(expected.keySet(), found);
	}

	public static void requiredApplicationComponents(WebApplicationContext context) {
		Set<String> expected = new HashSet<>(Arrays.asList("hexa.erp.partner.controller.PartnerController",
				"hexa.erp.warehouse.controller.WarehouseController", "hexa.erp.item.controller.ItemController",
				"hexa.erp.quotation.controller.QuotationController",
				"hexa.erp.salesorder.controller.SalesOrderController", "hexa.erp.sale.controller.SaleController",
				"hexa.erp.sale.controller.SaleStatusController", "hexa.erp.sale.controller.SalePriceController",
				"hexa.erp.shipinstruction.controller.ShipInstructionController",
				"hexa.erp.shipment.controller.ShipmentController", "hexa.erp.common.controller.HomeController",
				"hexa.erp.common.controller.CommonViewAdvice"));
		for (String[] route : LOOKUPS) {
			expected.add(route[1]);
		}
		Set<String> actual = new HashSet<>();
		for (Object bean : context.getBeansWithAnnotation(Component.class).values()) {
			actual.add(bean.getClass().getName());
		}
		for (String component : expected) {
			assertTrue(component, actual.contains(component));
		}
	}

	private static String owner(String path) {
		if (path.startsWith("/master/partner"))
			return "hexa.erp.partner.controller.PartnerController";
		if (path.startsWith("/master/warehouse"))
			return "hexa.erp.warehouse.controller.WarehouseController";
		if (path.startsWith("/master/item"))
			return "hexa.erp.item.controller.ItemController";
		if (path.startsWith("/quotation/"))
			return "hexa.erp.quotation.controller.QuotationController";
		if (path.startsWith("/order/"))
			return "hexa.erp.salesorder.controller.SalesOrderController";
		if (path.equals("/sale/status"))
			return "hexa.erp.sale.controller.SaleStatusController";
		if (path.equals("/sale/bulk-price") || path.equals("/sale/bulk-price/save"))
			return "hexa.erp.sale.controller.SalePriceController";
		if (path.startsWith("/sale/"))
			return "hexa.erp.sale.controller.SaleController";
		if (path.startsWith("/shipping-instruction/"))
			return "hexa.erp.shipinstruction.controller.ShipInstructionController";
		if (path.startsWith("/shipment/"))
			return "hexa.erp.shipment.controller.ShipmentController";
		throw new AssertionError("테스트에 등록되지 않은 화면: " + path);
	}
}
