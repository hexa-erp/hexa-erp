package hexa.erp.testinfra;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import hexa.erp.item.domain.ItemLookupVO;
import hexa.erp.item.service.ItemLookupService;
import hexa.erp.common.domain.LookupCriteria;

/** Webコンテキストテスト専用レスポンス。実際のDBデータや検索処理ではなく、mainでは使用しない。 */
public class StubItemLookupService implements ItemLookupService {
	@Override
	public ItemLookupVO get(Long id) {
		ItemLookupVO row = getList(new LookupCriteria(), null).get(0);
		row.setItemId(id);
		row.setItemName("Webテスト品目 " + id);
		return row;
	}

	@Override
	public int getTotal(LookupCriteria criteria) {
		return 1;
	}

	@Override
	public List<ItemLookupVO> getList(LookupCriteria criteria, Long warehouseId) {
		ItemLookupVO row = new ItemLookupVO();
		row.setItemId(42L);
		row.setItemCode("00001");
		row.setItemName("Webテスト品目");
		row.setSpecification("選択テスト規格");
		row.setUnit("EA");
		row.setOutboundPrice(new BigDecimal("100.00"));
		row.setStockQuantity(warehouseId == null ? null : new BigDecimal("12.375"));
		row.setActiveFlag("Y");
		return Collections.singletonList(row);
	}
}
