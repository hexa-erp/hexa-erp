package hexa.erp.shipment.service;

import java.util.List;

import hexa.erp.shipment.domain.ShipmentCriteria;
import hexa.erp.shipment.domain.ShipmentVO;

public interface ShipmentService {
	List<ShipmentVO> getList(ShipmentCriteria criteria);
	
	int getTotal(ShipmentCriteria criteria);
	
	ShipmentVO get(Long shipmentId);
	
	Long save(ShipmentVO shipment);
	
	int remove(List<Long> selectedIds);
	
	int changeStatus(List<Long> selectedIds, String progressStatus);

}
