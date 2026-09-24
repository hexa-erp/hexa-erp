package hexa.erp.testinfra;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

/** Stub 부모를 사용하는 웹 테스트의 기대 계약. JSP를 렌더링하거나 DB에 접속하지 않는다. */
public final class ControllerWebAssertions {
	private ControllerWebAssertions() {
	}

	private static final String[][] VIEWS = {
			{ "/master/partner", "master/partner", "partnerList" },
			{ "/master/warehouse", "master/warehouse", "warehouseList" },
			{ "/master/item", "master/item", "itemList" },
			{ "/quotation/list", "quotation/list", "quotationList" },
			{ "/quotation/form", "quotation/form", "form" },
			{ "/quotation/status", "quotation/status", "quotationStatusList" },
			{ "/quotation/statement", "quotation/statement", "form" },
			{ "/order/list", "order/list", "orderList" },
			{ "/order/form", "order/form", "form" },
			{ "/order/status", "order/status", "orderStatusList" },
			{ "/order/statement", "order/statement", "form" },
			{ "/sale/list", "sale/list", "saleList" },
			{ "/sale/form", "sale/form", "form" },
			{ "/sale/status", "sale/status", "saleStatusList" },
			{ "/sale/statement", "sale/statement", "form" },
			{ "/shipping-instruction/list", "shippingInstruction/list", "shipInstructionList" },
			{ "/shipping-instruction/form", "shippingInstruction/form", "form" },
			{ "/shipment/list", "shipment/list", "shipmentList" },
			{ "/shipment/form", "shipment/form", "form" },
			{ "/quotation/unordered", "quotation/unordered", "unorderedList" },
			{ "/order/unsold", "order/unsold", "unsoldList" },
			{ "/sale/bulk-price", "sale/bulk-price", "salePriceList" }
	};
	private static final String[][] LOOKUPS = {
			{ "/lookup/options/partner", "hexa.erp.partner.controller.PartnerLookupController" },
			{ "/lookup/options/warehouse", "hexa.erp.warehouse.controller.WarehouseLookupController" },
			{ "/lookup/options/item", "hexa.erp.item.controller.ItemLookupController" },
			{ "/lookup/options/assignee", "hexa.erp.assignee.controller.AssigneeLookupController" },
			{ "/lookup/sources/quotation", "hexa.erp.quotation.controller.QuotationLookupController" },
			{ "/lookup/sources/order", "hexa.erp.salesorder.controller.SalesOrderLookupController" },
			{ "/lookup/sources/sale", "hexa.erp.sale.controller.SaleLookupController" },
			{ "/lookup/sources/shipping-instruction", "hexa.erp.shipinstruction.controller.ShipInstructionLookupController" }
	};
	private static final String[] WRITES = {
			"/master/partner/save",
			"/master/partner/active",
			"/master/warehouse/save",
			"/master/warehouse/active",
			"/master/item/save",
			"/master/item/active",
			"/master/item/stock",
			"/quotation/save",
			"/quotation/delete",
			"/quotation/change-status",
			"/order/save",
			"/order/delete",
			"/order/change-status",
			"/sale/save",
			"/sale/delete",
			"/sale/change-status",
			"/shipping-instruction/save",
			"/shipping-instruction/delete",
			"/shipping-instruction/change-status",
			"/shipment/save",
			"/shipment/delete",
			"/shipment/change-status",
			"/sale/bulk-price/save"
	};

	public static void emptyViews(MockMvc mvc) throws Exception {
		for (String[] route : VIEWS) {
			MvcResult result = mvc.perform(get(route[0]).servletPath(route[0])).andReturn();
			assertEquals(route[0], 200, result.getResponse().getStatus());
			ModelAndView mv = result.getModelAndView();
			assertNotNull(route[0], mv);
			assertEquals(route[1], mv.getViewName());
			Map<String, Object> model = mv.getModel();
			assertEquals(route[0], model.get("currentPath"));
			assertEquals(Boolean.TRUE, model.get("developmentMode"));
			assertNotNull(model.get("navigation"));
			assertNotNull(model.get("connectedAt"));
			assertNotNull(model.get("pageTitle"));
			assertNotNull(route[2], model.get(route[2]));
			if ("form".equals(route[2])) {
				Map<?, ?> form = (Map<?, ?>) model.get("form");
				assertTrue(((List<?>) form.get("lines")).isEmpty());
				if (route[0].endsWith("/form")) {
					assertEquals("create", model.get("mode"));
					assertEquals(Boolean.FALSE, model.get("isEdit"));
				} else {
					assertTrue(((Map<?, ?>) model.get("statementTotals")).isEmpty());
				}
			} else {
				assertTrue(((List<?>) model.get(route[2])).isEmpty());
				assertNotNull(model.get("search"));
				boolean report = route[0].endsWith("/status") || route[0].endsWith("/unordered")
						|| route[0].endsWith("/unsold");
				if (report) {
					assertTrue(((List<?>) model.get("reportMonths")).isEmpty());
					assertTrue(((Map<?, ?>) model.get("statusTotals")).isEmpty());
					assertFalse(model.containsKey("page"));
					assertFalse(model.containsKey("pageSize"));
				} else {
					assertEquals(1, model.get("page"));
					assertEquals(1, model.get("totalPages"));
					assertEquals(0, model.get("totalCount"));
					assertEquals(route[0].startsWith("/master/") ? 50 : 25, model.get("pageSize"));
					assertEquals(route[0], model.get("basePath"));
				}
			}
		}
	}

	public static void editReadsAreExplicitlyUnimplemented(MockMvc mvc) throws Exception {
		String[][] forms = { { "quotation", "quotationId" }, { "order", "salesOrderId" },
				{ "sale", "saleId" }, { "shipping-instruction", "shipInstructionId" },
				{ "shipment", "shipmentId" } };
		for (String[] formRoute : forms) {
			for (String parameter : Arrays.asList("id", formRoute[1])) {
				MvcResult result = mvc.perform(get("/" + formRoute[0] + "/form")
						.param(parameter, "000999999999999999999")).andReturn();
				assertEquals(501, result.getResponse().getStatus());
				Map<String, Object> model = result.getModelAndView().getModel();
				assertEquals("", ((Map<?, ?>) model.get("form")).get(formRoute[1]));
				assertEquals("create", model.get("mode"));
				assertEquals(Boolean.FALSE, model.get("isEdit"));
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
			MvcResult result = mvc.perform(get(url).param("keyword", "001")
					.param("page", "2").param("progressStatus", "IN_PROGRESS")).andReturn();
			assertEquals(url, 200, result.getResponse().getStatus());
			assertTrue(result.getHandler() instanceof HandlerMethod);
			assertEquals(url, route[1], ((HandlerMethod) result.getHandler()).getBeanType().getName());
			JsonNode body = json.readTree(result.getResponse().getContentAsString());
			assertEquals(5, body.size());
			assertTrue(body.get("rows").isArray());
			assertEquals(0, body.get("rows").size());
			assertEquals(1, body.get("page").asInt());
			assertEquals(1, body.get("totalPages").asInt());
			assertEquals(0, body.get("totalCount").asInt());
			assertEquals(25, body.get("pageSize").asInt());
		}
		assertEquals(404, mvc.perform(get("/lookup/options/unknown")).andReturn().getResponse().getStatus());
		assertEquals(404, mvc.perform(get("/lookup/sources/unknown")).andReturn().getResponse().getStatus());
		assertEquals(404, mvc.perform(get("/unknown/list")).andReturn().getResponse().getStatus());
	}

	public static void writesAreBlocked(MockMvc mvc) throws Exception {
		ObjectMapper json = new ObjectMapper();
		for (String url : WRITES) {
			MvcResult result = mvc.perform(post(url).param("id", "001")
					.param("selectedIds", "001", "002")).andReturn();
			assertEquals(url, 501, result.getResponse().getStatus());
			JsonNode body = json.readTree(result.getResponse().getContentAsString());
			assertFalse(body.get("saved").asBoolean());
			assertEquals("아직 구현되지 않은 기능입니다. 실제 데이터는 저장되지 않았습니다.",
					body.get("message").asText());
		}
	}

	public static void searchConditions(MockMvc mvc) throws Exception {
		String[] urls = { "/quotation/status", "/quotation/unordered", "/order/status",
				"/order/unsold", "/sale/status", "/sale/bulk-price" };
		for (String url : urls) {
			for (String view : Arrays.asList("search", "results")) {
				MvcResult result = mvc.perform(get(url).param("view", view).param("keyword", "보존 확인")
						.param("datePreset", "custom").param("startDate", "2026-01-01")
						.param("endDate", "2026-02-28").param("progressStatus", "IN_PROGRESS")
						.param("page", "7").param("pageSize", "999")
						.param("warehouseIds", "001", "002").param("partnerIds", "0007", "9007199254740993")
						.param("itemIds", "I-001", "I-002").param("assigneeIds", "A-001", "A-002")).andReturn();
				assertEquals(200, result.getResponse().getStatus());
				Map<String, Object> model = result.getModelAndView().getModel();
				Map<?, ?> search = (Map<?, ?>) model.get("search");
				assertEquals("보존 확인", search.get("keyword"));
				assertEquals("IN_PROGRESS", search.get("progressStatus"));
				assertEquals("2026-01-01", search.get("startDate"));
				assertEquals("2026-02-28", search.get("endDate"));
				assertEquals(view, search.get("view"));
				Map<?, ?> ids = (Map<?, ?>) model.get("filterIds");
				assertEquals(Arrays.asList("001", "002"), ids.get("warehouseIds"));
				assertEquals(Arrays.asList("0007", "9007199254740993"), ids.get("partnerIds"));
				assertEquals(Arrays.asList("I-001", "I-002"), ids.get("itemIds"));
				assertFalse(search.containsKey("partnerIds"));
				if (url.endsWith("bulk-price")) {
					assertFalse(ids.containsKey("assigneeIds"));
					assertFalse(search.containsKey("assigneeIds"));
				} else {
					assertEquals(Arrays.asList("A-001", "A-002"), ids.get("assigneeIds"));
					assertFalse(search.containsKey("page"));
					assertFalse(search.containsKey("pageSize"));
				}
			}
		}
	}

	public static void exactMappings(RequestMappingHandlerMapping mapping) {
		Map<String, String> actual = new LinkedHashMap<>();
		for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mapping.getHandlerMethods().entrySet()) {
			assertEquals(1, entry.getKey().getMethodsCondition().getMethods().size());
			for (String path : entry.getKey().getPatternsCondition().getPatterns()) {
				assertFalse(path.contains("{") || path.contains("*"));
				for (RequestMethod method : entry.getKey().getMethodsCondition().getMethods()) {
					String key = method.name() + " " + path;
					assertNull("중복 매핑: " + key, actual.put(key, entry.getValue().getBeanType().getName()));
				}
			}
		}
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
		assertEquals(54, expected.size());
		assertEquals(expected, actual);
	}

	public static void onlyExpectedApplicationComponents(WebApplicationContext context) {
		Set<String> expected = new HashSet<>(Arrays.asList(
				"hexa.erp.partner.controller.PartnerController",
				"hexa.erp.warehouse.controller.WarehouseController",
				"hexa.erp.item.controller.ItemController",
				"hexa.erp.quotation.controller.QuotationController",
				"hexa.erp.salesorder.controller.SalesOrderController",
				"hexa.erp.sale.controller.SaleController",
				"hexa.erp.shipinstruction.controller.ShipInstructionController",
				"hexa.erp.shipment.controller.ShipmentController",
				"hexa.erp.common.controller.HomeController",
				"hexa.erp.common.controller.CommonViewAdvice"));
		for (String[] route : LOOKUPS) {
			expected.add(route[1]);
		}
		Set<String> actual = new HashSet<>();
		for (Object bean : context.getBeansWithAnnotation(Component.class).values()) {
			actual.add(bean.getClass().getName());
		}
		assertEquals(expected, actual);
	}

	private static String owner(String path) {
		if (path.startsWith("/master/partner")) return "hexa.erp.partner.controller.PartnerController";
		if (path.startsWith("/master/warehouse")) return "hexa.erp.warehouse.controller.WarehouseController";
		if (path.startsWith("/master/item")) return "hexa.erp.item.controller.ItemController";
		if (path.startsWith("/quotation/")) return "hexa.erp.quotation.controller.QuotationController";
		if (path.startsWith("/order/")) return "hexa.erp.salesorder.controller.SalesOrderController";
		if (path.startsWith("/sale/")) return "hexa.erp.sale.controller.SaleController";
		if (path.startsWith("/shipping-instruction/")) return "hexa.erp.shipinstruction.controller.ShipInstructionController";
		if (path.startsWith("/shipment/")) return "hexa.erp.shipment.controller.ShipmentController";
		throw new AssertionError("테스트에 등록되지 않은 화면: " + path);
	}
}
