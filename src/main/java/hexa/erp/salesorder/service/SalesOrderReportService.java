package hexa.erp.salesorder.service;

import hexa.erp.salesorder.domain.SalesOrderReportCriteria;
import hexa.erp.salesorder.domain.SalesOrderReportVO;

public interface SalesOrderReportService {

	SalesOrderReportVO getStatus(SalesOrderReportCriteria criteria);
}
