package hexa.erp.testinfra;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import hexa.erp.item.domain.ItemLookupVO;
import hexa.erp.item.service.ItemLookupService;
import hexa.erp.common.domain.LookupCriteria;

/** 웹 컨텍스트 테스트 전용 응답. 실제 DB 데이터나 검색 구현이 아니며 main에서는 사용하지 않는다. */
public class StubItemLookupService implements ItemLookupService {
	@Override
	public ItemLookupVO get(Long id) {
		ItemLookupVO row = getList(new LookupCriteria(), null).get(0);
		row.setItemId(id);
		row.setItemName("웹 테스트 품목 " + id);
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
		row.setItemName("웹 테스트 품목");
		row.setSpecification("선택 테스트 규격");
		row.setUnit("EA");
		row.setOutboundPrice(new BigDecimal("100.00"));
		row.setStockQuantity(warehouseId == null ? null : new BigDecimal("12.375"));
		row.setActiveFlag("Y");
		return Collections.singletonList(row);
	}
}
