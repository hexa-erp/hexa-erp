package hexa.erp.warehouse.service;

import java.util.List;

import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.common.domain.LookupCriteria;

public interface WarehouseLookupService {
	WarehouseLookupVO get(Long warehouseId);

	List<WarehouseLookupVO> getList(LookupCriteria criteria);

	int getTotal(LookupCriteria criteria);
}
