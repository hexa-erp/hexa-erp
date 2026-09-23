package hexa.erp.common.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class LookupResponsesTest {
	@Test
	public void emptyEnvelopeKeepsExistingContract() {
		Map<String, Object> result = LookupResponses.emptyResult();
		assertEquals(Arrays.asList("rows", "page", "totalPages", "totalCount", "pageSize"),
				new ArrayList<>(result.keySet()));
		assertTrue(result.get("rows") instanceof List);
		assertTrue(((List<?>) result.get("rows")).isEmpty());
		assertEquals(1, result.get("page"));
		assertEquals(1, result.get("totalPages"));
		assertEquals(0, result.get("totalCount"));
		assertEquals(25, result.get("pageSize"));
	}

	@Test
	public void eachRequestReceivesItsOwnEnvelope() {
		Map<String, Object> first = LookupResponses.emptyResult();
		Map<String, Object> second = LookupResponses.emptyResult();
		assertNotSame(first, second);
		first.put("rows", Collections.singletonList("테스트 변경"));
		first.put("page", 9);
		assertTrue(((List<?>) second.get("rows")).isEmpty());
		assertEquals(1, second.get("page"));
	}
}
