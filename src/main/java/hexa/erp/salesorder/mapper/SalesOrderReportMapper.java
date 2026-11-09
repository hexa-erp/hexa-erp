package hexa.erp.salesorder.mapper;

import java.util.List;

import hexa.erp.salesorder.domain.SalesOrderReportCriteria;
import hexa.erp.salesorder.domain.SalesOrderReportRowVO;

public interface SalesOrderReportMapper {

	// 検索条件に一致する受注集計を品目別に取得する。
	List<SalesOrderReportRowVO> getStatusRows(SalesOrderReportCriteria criteria);

	// 検索条件に一致する未売上一覧を品目別に取得する。
	List<SalesOrderReportRowVO> getUnsoldRows(SalesOrderReportCriteria criteria);
}
