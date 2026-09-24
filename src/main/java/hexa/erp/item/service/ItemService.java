package hexa.erp.item.service;

import java.util.List;

import hexa.erp.item.domain.ItemVO;
import hexa.erp.common.domain.LookupCriteria;

public interface ItemService {
	List<ItemVO> getList(LookupCriteria criteria, Long warehouseId);

	int getTotal(LookupCriteria criteria);
}
