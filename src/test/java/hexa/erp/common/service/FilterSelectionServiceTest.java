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
		assertNames(selected.get("warehouse"), "Webテスト倉庫 ", "202", "201");
		assertSelection(selected.get("partner").get(0), "001", "00001", "Webテスト取引先 1");
		assertSelection(selected.get("partner").get(1), "9007199254740993", "00001", "Webテスト取引先 9007199254740993");
		assertNames(selected.get("item"), "Webテスト品目 ", "301", "302");
		assertNames(selected.get("assignee"), "Webテスト担当者 ", "401", "402");
		assertEquals(Arrays.asList("001", "9007199254740993"), ids.get("partnerIds"));
	}

	@Test
	public void inactiveSelectionKeepsItsRealNameAndCode() {
		service.setPartnerLookupService(new StubPartnerLookupService() {
			@Override
			public PartnerLookupVO get(Long id) {
				PartnerLookupVO row = super.get(id);
				row.setActiveFlag("N");
				row.setPartnerName("利用停止取引先");
				return row;
			}
		});

		FilterSelectionVO selected = service.getSelections(Collections.singletonMap("partnerIds", Arrays.asList("101")))
				.get("partner").get(0);

		assertSelection(selected, "101", "00001", "利用停止取引先");
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
			assertSelection(selected.get(i), id, "", "見つからない項目 (" + id + ")");
		}
		assertEquals(Arrays.asList(999L), queried);
	}

	@Test
	public void databaseFailurePropagatesInsteadOfBecomingAMissingSelection() {
		final RuntimeException failure = new IllegalStateException("テスト用取得失敗");
		service.setPartnerLookupService(new StubPartnerLookupService() {
			@Override
			public PartnerLookupVO get(Long id) {
				throw failure;
			}
		});

		try {
			service.getSelections(Collections.singletonMap("partnerIds", Arrays.asList("101")));
			fail("1件取得の失敗を項目が見つからない状態に置き換えてはならない。");
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
