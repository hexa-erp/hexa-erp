package hexa.erp.common.controller;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;

public class ViewModelsTest {
	@Test
	public void searchCopiesRatherThanMutatingRequestValues() {
		Map<String, String> params = new LinkedHashMap<>();
		params.put("keyword", "001 검색");
		params.put("view", "results");
		Map<String, String> search = ViewModels.search(params);
		assertEquals("001 검색", search.get("keyword"));
		assertEquals("results", search.get("view"));
		assertEquals("custom", search.get("datePreset"));
		assertEquals("", search.get("progressStatus"));
		assertEquals(2, params.size());
	}

	@Test
	public void repeatedIdsStayStringsAndNeverBecomeCodes() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("partnerIds", "001", "9007199254740993", "001", "", " A-02 ");
		ExtendedModelMap model = new ExtendedModelMap();
		Map<String, String> search = new LinkedHashMap<>();
		search.put("partnerId", "ignored-legacy");
		search.put("partnerName", "조회되지 않은 이름");
		ViewModels.filters(request, search, model, "warehouse", "partner", "item", "assignee");
		assertEquals(Arrays.asList("001", "9007199254740993", "A-02"),
				((Map<?, ?>) model.get("filterIds")).get("partnerIds"));
		List<?> tags = (List<?>) ((Map<?, ?>) model.get("filterSelections")).get("partner");
		for (Object value : tags) {
			Map<?, ?> tag = (Map<?, ?>) value;
			assertEquals("", tag.get("code"));
			assertEquals("ID " + tag.get("id"), tag.get("name"));
		}
		assertFalse(search.containsKey("partnerId"));
		assertFalse(search.containsKey("partnerName"));
	}

	@Test
	public void explicitEmptyPluralWinsAndLegacySingleIdStillWorks() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("partnerIds", "");
		Map<String, String> search = new LinkedHashMap<>();
		search.put("partnerId", "001");
		search.put("itemId", "0009");
		ExtendedModelMap model = new ExtendedModelMap();
		ViewModels.filters(request, search, model, "partner", "item");
		Map<?, ?> ids = (Map<?, ?>) model.get("filterIds");
		assertTrue(((List<?>) ids.get("partnerIds")).isEmpty());
		assertEquals(Arrays.asList("0009"), ids.get("itemIds"));
	}

	@Test
	public void bulkPriceHasNoAssigneeFilterEvenForOldLinks() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("assigneeIds", "001", "002");
		Map<String, String> search = new LinkedHashMap<>();
		search.put("assigneeIds", "001");
		search.put("assigneeId", "001");
		search.put("assigneeName", "담당자");
		ExtendedModelMap model = new ExtendedModelMap();
		ViewModels.filters(request, search, model, "warehouse", "partner", "item");
		assertTrue(search.isEmpty());
		assertFalse(((Map<?, ?>) model.get("filterIds")).containsKey("assigneeIds"));
		assertFalse(((Map<?, ?>) model.get("filterSelections")).containsKey("assignee"));
	}

	@Test
	public void emptyPageOnlyProvidesDisplayDefaults() {
		ExtendedModelMap model = new ExtendedModelMap();
		ViewModels.emptyPage(model, 50, "/master/partner");
		assertEquals(1, model.get("page"));
		assertEquals(1, model.get("totalPages"));
		assertEquals(0, model.get("totalCount"));
		assertEquals(50, model.get("pageSize"));
		assertEquals("/master/partner", model.get("basePath"));
		assertEquals(5, model.size());
	}
}
