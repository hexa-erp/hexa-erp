package hexa.erp.sale.service;

import hexa.erp.sale.domain.SaleStatusCriteria;
import hexa.erp.sale.domain.SaleStatusVO;

/** 売上集計の業務ロジック。 */
public interface SaleStatusService {
	/** 検索結果を月別にまとめ、合計を計算して返す。 */
	SaleStatusVO getStatus(SaleStatusCriteria criteria);

}
