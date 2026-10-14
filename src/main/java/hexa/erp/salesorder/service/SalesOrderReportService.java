package hexa.erp.salesorder.service;

import hexa.erp.salesorder.domain.SalesOrderReportCriteria;
import hexa.erp.salesorder.domain.SalesOrderReportVO;

public interface SalesOrderReportService {

	//주문서 현황 조회 및 월별 합계
	SalesOrderReportVO getStatus(SalesOrderReportCriteria criteria);
	
	//미판매 현황 조회
	SalesOrderReportVO getUnsold(SalesOrderReportCriteria criteria);
}
