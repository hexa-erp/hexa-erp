package hexa.erp.warehouse.mapper;

import java.util.List;

import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.common.domain.LookupCriteria;

public interface WarehouseLookupMapper {
	WarehouseLookupVO read(Long warehouseId);

	List<WarehouseLookupVO> getListWithPaging(LookupCriteria criteria);

	int getTotalCount(LookupCriteria criteria);
}
