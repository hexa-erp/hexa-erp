package hexa.erp.salesorder.service;

import hexa.erp.salesorder.domain.SalesOrderVO;

public interface SalesOrderService {
	
	SalesOrderVO get(Long salesOrderId);
	
	Long save(SalesOrderVO salesOrder);
	
	
}
