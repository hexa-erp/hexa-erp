package hexa.erp.warehouse.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.mapper.WarehouseMapper;
import hexa.erp.common.domain.LookupCriteria;

@Service
public class WarehouseServiceImpl implements WarehouseService {
	private WarehouseMapper mapper;

	@Autowired
	public void setMapper(WarehouseMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public List<WarehouseVO> getList(LookupCriteria criteria) {
		return mapper.getList(criteria);
	}

	@Override
	public int getTotal(LookupCriteria criteria) {
		return mapper.getTotal(criteria);
	}
}
