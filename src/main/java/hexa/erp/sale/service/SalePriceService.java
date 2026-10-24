package hexa.erp.sale.service;

import java.util.List;

import hexa.erp.sale.domain.SalePriceChangeVO;
import hexa.erp.sale.domain.SalePriceCriteria;
import hexa.erp.sale.domain.SalePriceVO;

/** 売上単価一括変更の業務ロジック。 */
public interface SalePriceService {
	/** 検索条件に一致する品目行を1ページ分取得する。 */
	List<SalePriceVO> getList(SalePriceCriteria criteria);

	/** 検索条件に一致する品目行の総件数。ページ数の計算に使用する。 */
	int getTotal(SalePriceCriteria criteria);

	/**
	 * 選択した品目行の単価を変更し、税抜金額・消費税・合計を再計算して保存する。1件でも失敗した場合はすべて取り消し、
	 * IllegalArgumentExceptionを送出する。変更件数を返す。
	 */
	int savePrices(List<SalePriceChangeVO> changes);

}
