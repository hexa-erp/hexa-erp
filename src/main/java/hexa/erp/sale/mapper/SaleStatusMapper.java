package hexa.erp.sale.mapper;

import java.util.List;

import hexa.erp.sale.domain.SaleStatusCriteria;
import hexa.erp.sale.domain.SaleStatusRowVO;

/** 売上集計を取得する。削除されていない伝票と品目行のみを対象とする。 */
public interface SaleStatusMapper {
	/** 検索条件に一致するすべての品目行を日付順に取得する。ページ分割は行わない。 */
	List<SaleStatusRowVO> getStatusRows(SaleStatusCriteria criteria);

}
