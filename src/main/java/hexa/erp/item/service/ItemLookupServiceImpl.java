package hexa.erp.item.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.item.domain.ItemLookupVO;
import hexa.erp.item.mapper.ItemLookupMapper;
import hexa.erp.common.domain.LookupCriteria;
import lombok.Setter;

@Service
public class ItemLookupServiceImpl implements ItemLookupService {
	@Setter(onMethod_ = @Autowired)
	private ItemLookupMapper mapper;

	@Override
	public ItemLookupVO get(Long itemId) {
		return mapper.read(itemId);
	}

	@Override
	public List<ItemLookupVO> getList(LookupCriteria criteria, Long warehouseId) {
		return mapper.getListWithPaging(criteria, warehouseId);
	}

	@Override
	public int getTotal(LookupCriteria criteria) {
		return mapper.getTotalCount(criteria);
	}
}
