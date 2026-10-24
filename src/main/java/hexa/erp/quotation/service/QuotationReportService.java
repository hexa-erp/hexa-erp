package hexa.erp.quotation.service;

import hexa.erp.quotation.domain.QuotationReportCriteria;
import hexa.erp.quotation.domain.QuotationReportVO;

public interface QuotationReportService {

	// 見積集計の取得と月別合計
	QuotationReportVO getStatus(QuotationReportCriteria criteria);

	// 未受注一覧を取得する。
	QuotationReportVO getUnordered(QuotationReportCriteria criteria);
}
