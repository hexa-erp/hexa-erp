package hexa.erp.item.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.item.domain.ItemVO;
import hexa.erp.item.mapper.ItemMapper;
import hexa.erp.common.domain.LookupCriteria;

@Service
public class ItemServiceImpl implements ItemService {
	private ItemMapper mapper;

	@Autowired
	public void setMapper(ItemMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public List<ItemVO> getList(LookupCriteria criteria, Long warehouseId) {
		return mapper.getList(criteria, warehouseId);
	}

	@Override
	public int getTotal(LookupCriteria criteria) {
		return mapper.getTotal(criteria);
	}
}
