package hexa.erp.quotation.mapper;

import java.util.List;

import hexa.erp.quotation.domain.QuotationReportCriteria;
import hexa.erp.quotation.domain.QuotationReportRowVO;

public interface QuotationReportMapper {

	// 検索条件に一致する見積集計を品目別に取得する。
	List<QuotationReportRowVO> getStatusRows(QuotationReportCriteria criteria);

	// 検索条件に一致する未受注一覧を品目別に取得する。
	List<QuotationReportRowVO> getUnorderedRows(QuotationReportCriteria criteria);
}
