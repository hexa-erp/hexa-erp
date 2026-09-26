package hexa.erp.common.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import hexa.erp.common.domain.FilterSelectionVO;
import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.testinfra.StubAssigneeLookupService;
import hexa.erp.testinfra.StubItemLookupService;
import hexa.erp.testinfra.StubPartnerLookupService;
import hexa.erp.testinfra.StubWarehouseLookupService;

public class FilterSelectionServiceTest {
	private FilterSelectionServiceImpl service;

	@Before
	public void setUp() {
		service = new FilterSelectionServiceImpl();
		service.setAssigneeLookupService(new StubAssigneeLookupService());
		service.setPartnerLookupService(new StubPartnerLookupService());
		service.setWarehouseLookupService(new StubWarehouseLookupService());
		service.setItemLookupService(new StubItemLookupService());
	}

	@Test
	public void restoresNamesAndCodesInSelectionOrderWithoutChangingStringIds() {
		Map<String, List<String>> ids = new LinkedHashMap<>();
		ids.put("warehouseIds", Arrays.asList("202", "201"));
		ids.put("partnerIds", Arrays.asList("001", "9007199254740993"));
		ids.put("itemIds", Arrays.asList("301", "302"));
		ids.put("assigneeIds", Arrays.asList("401", "402"));

		Map<String, List<FilterSelectionVO>> selected = service.getSelections(ids);

		assertEquals(4, selected.size());
		assertNames(selected.get("warehouse"), "웹 테스트 창고 ", "202", "201");
		assertSelection(selected.get("partner").get(0), "001", "00001", "웹 테스트 거래처 1");
		assertSelection(selected.get("partner").get(1), "9007199254740993", "00001", "웹 테스트 거래처 9007199254740993");
		assertNames(selected.get("item"), "웹 테스트 품목 ", "301", "302");
		assertNames(selected.get("assignee"), "웹 테스트 담당자 ", "401", "402");
		assertEquals(Arrays.asList("001", "9007199254740993"), ids.get("partnerIds"));
	}

	@Test
	public void inactiveSelectionKeepsItsRealNameAndCode() {
		service.setPartnerLookupService(new StubPartnerLookupService() {
			@Override
			public PartnerLookupVO get(Long id) {
				PartnerLookupVO row = super.get(id);
				row.setActiveFlag("N");
				row.setPartnerName("사용 중지 거래처");
				return row;
			}
		});

		FilterSelectionVO selected = service.getSelections(Collections.singletonMap("partnerIds", Arrays.asList("101")))
				.get("partner").get(0);

		assertSelection(selected, "101", "00001", "사용 중지 거래처");
	}

	@Test
	public void missingAndMalformedIdsRemainVisibleAndMalformedIdsNeverQuery() {
		final List<Long> queried = new ArrayList<>();
		service.setPartnerLookupService(new StubPartnerLookupService() {
			@Override
			public PartnerLookupVO get(Long id) {
				queried.add(id);
				return null;
			}
		});
		List<String> ids = Arrays.asList("999", "A-02", "9223372036854775808");

		List<FilterSelectionVO> selected = service.getSelections(Collections.singletonMap("partnerIds", ids))
				.get("partner");

		assertEquals(ids.size(), selected.size());
		for (int i = 0; i < ids.size(); i++) {
			String id = ids.get(i);
			assertSelection(selected.get(i), id, "", "찾을 수 없는 항목 (" + id + ")");
		}
		assertEquals(Arrays.asList(999L), queried);
	}

	@Test
	public void databaseFailurePropagatesInsteadOfBecomingAMissingSelection() {
		final RuntimeException failure = new IllegalStateException("테스트용 조회 실패");
		service.setPartnerLookupService(new StubPartnerLookupService() {
			@Override
			public PartnerLookupVO get(Long id) {
				throw failure;
			}
		});

		try {
			service.getSelections(Collections.singletonMap("partnerIds", Arrays.asList("101")));
			fail("단건 조회 실패를 찾을 수 없는 항목으로 바꾸면 안 된다.");
		} catch (RuntimeException actual) {
			assertSame(failure, actual);
		}
	}

	private void assertNames(List<FilterSelectionVO> selected, String namePrefix, String... ids) {
		assertEquals(ids.length, selected.size());
		for (int i = 0; i < ids.length; i++) {
			assertSelection(selected.get(i), ids[i], "00001", namePrefix + ids[i]);
		}
	}

	private void assertSelection(FilterSelectionVO selected, String id, String code, String name) {
		assertEquals(id, selected.getId());
		assertEquals(code, selected.getCode());
		assertEquals(name, selected.getName());
	}
}
