package hexa.erp.testinfra;

import java.util.Collections;
import java.util.List;

import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.service.WarehouseService;
import hexa.erp.common.domain.LookupCriteria;

/** 웹 컨텍스트 테스트 전용 응답. 실제 DB 데이터나 검색 구현이 아니며 main에서는 사용하지 않는다. */
public class StubWarehouseService implements WarehouseService {
	@Override
	public int getTotal(LookupCriteria criteria) {
		return 1;
	}

	@Override
	public List<WarehouseVO> getList(LookupCriteria criteria) {
		WarehouseVO row = new WarehouseVO();
		row.setWarehouseId(42L);
		row.setWarehouseCode("00001");
		row.setWarehouseName("웹 테스트 창고");
		row.setWarehouseType("창고");
		row.setActiveFlag("Y");
		return Collections.singletonList(row);
	}
}
