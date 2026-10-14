package hexa.erp.salesorder.mapper;

import java.util.List;

import hexa.erp.salesorder.domain.SalesOrderReportCriteria;
import hexa.erp.salesorder.domain.SalesOrderReportRowVO;

public interface SalesOrderReportMapper {

	//검색조건에 따른 주문서 현황 품목 별 조회
	List<SalesOrderReportRowVO> getStatusRows(SalesOrderReportCriteria criteria);
	
	//검색조건에 따른 미판매 현황 품목 별 조회
	List<SalesOrderReportRowVO> getUnsoldRows(SalesOrderReportCriteria criteria);
}
