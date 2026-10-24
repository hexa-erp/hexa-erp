package hexa.erp.testinfra;

import java.util.Collections;
import java.util.List;

import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.warehouse.service.WarehouseLookupService;
import hexa.erp.common.domain.LookupCriteria;

/** Webコンテキストテスト専用レスポンス。実際のDBデータや検索処理ではなく、mainでは使用しない。 */
public class StubWarehouseLookupService implements WarehouseLookupService {
	@Override
	public WarehouseLookupVO get(Long id) {
		WarehouseLookupVO row = getList(new LookupCriteria()).get(0);
		row.setWarehouseId(id);
		row.setWarehouseName("Webテスト倉庫 " + id);
		return row;
	}

	@Override
	public int getTotal(LookupCriteria criteria) {
		return 1;
	}

	@Override
	public List<WarehouseLookupVO> getList(LookupCriteria criteria) {
		WarehouseLookupVO row = new WarehouseLookupVO();
		row.setWarehouseId(42L);
		row.setWarehouseCode("00001");
		row.setWarehouseName("Webテスト倉庫");
		row.setWarehouseType("倉庫");
		row.setActiveFlag("Y");
		return Collections.singletonList(row);
	}
}
