package hexa.erp.warehouse.mapper;

import java.util.List;

import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.common.domain.LookupCriteria;

public interface WarehouseMapper {
	List<WarehouseVO> getList(LookupCriteria criteria);

	int getTotal(LookupCriteria criteria);
}
