package hexa.erp.warehouse.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.warehouse.mapper.WarehouseLookupMapper;
import hexa.erp.common.domain.LookupCriteria;
import lombok.Setter;

@Service
public class WarehouseLookupServiceImpl implements WarehouseLookupService {
	@Setter(onMethod_ = @Autowired)
	private WarehouseLookupMapper mapper;

	@Override
	public WarehouseLookupVO get(Long warehouseId) {
		return mapper.read(warehouseId);
	}

	@Override
	public List<WarehouseLookupVO> getList(LookupCriteria criteria) {
		return mapper.getListWithPaging(criteria);
	}

	@Override
	public int getTotal(LookupCriteria criteria) {
		return mapper.getTotalCount(criteria);
	}
}
