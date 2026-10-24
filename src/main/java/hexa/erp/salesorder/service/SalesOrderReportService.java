package hexa.erp.salesorder.service;

import hexa.erp.salesorder.domain.SalesOrderReportCriteria;
import hexa.erp.salesorder.domain.SalesOrderReportVO;

public interface SalesOrderReportService {

	// 受注集計の取得と月別合計
	SalesOrderReportVO getStatus(SalesOrderReportCriteria criteria);

	// 未売上一覧を取得する。
	SalesOrderReportVO getUnsold(SalesOrderReportCriteria criteria);
}
