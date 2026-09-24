package hexa.erp.testinfra;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import hexa.erp.item.domain.ItemVO;
import hexa.erp.item.service.ItemService;
import hexa.erp.common.domain.LookupCriteria;

/** 웹 컨텍스트 테스트 전용 응답. 실제 DB 데이터나 검색 구현이 아니며 main에서는 사용하지 않는다. */
public class StubItemService implements ItemService {
	@Override
	public int getTotal(LookupCriteria criteria) {
		return 1;
	}

	@Override
	public List<ItemVO> getList(LookupCriteria criteria, Long warehouseId) {
		ItemVO row = new ItemVO();
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
