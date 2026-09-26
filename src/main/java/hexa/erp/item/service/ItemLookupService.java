package hexa.erp.item.service;

import java.util.List;

import hexa.erp.item.domain.ItemLookupVO;
import hexa.erp.common.domain.LookupCriteria;

public interface ItemLookupService {
	ItemLookupVO get(Long itemId);

	List<ItemLookupVO> getList(LookupCriteria criteria, Long warehouseId);

	int getTotal(LookupCriteria criteria);
}
