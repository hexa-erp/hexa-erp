package hexa.erp.salesorder.service;

import java.util.List;

import hexa.erp.salesorder.domain.SalesOrderCriteria;
import hexa.erp.salesorder.domain.SalesOrderVO;

public interface SalesOrderService {

	List<SalesOrderVO> getList(SalesOrderCriteria criteria);

	int getTotal(SalesOrderCriteria criteria);

	SalesOrderVO get(Long salesOrderId);

	Long save(SalesOrderVO salesOrder);

	int remove(List<Long> selectedIds);

	int changeStatus(List<Long> selectedIds, String progressStatus);
}
