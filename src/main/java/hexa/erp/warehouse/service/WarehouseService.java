package hexa.erp.warehouse.service;

import java.util.List;

import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.common.domain.LookupCriteria;

public interface WarehouseService {
	List<WarehouseVO> getList(LookupCriteria criteria);

	int getTotal(LookupCriteria criteria);
}
