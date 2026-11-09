package hexa.erp.sale.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.sale.domain.SalePriceCriteria;
import hexa.erp.sale.domain.SalePriceVO;

/** 売上単価の一括変更。削除されていない伝票と品目行のみ取得・更新する。 */
public interface SalePriceMapper {
	/** 検索条件に一致する品目行を日付の降順で1ページ分取得する。 */
	List<SalePriceVO> getListWithPaging(SalePriceCriteria criteria);

	/** ページ数の計算用。検索条件に一致する品目行の総件数を取得する。 */
	int getTotalCount(SalePriceCriteria criteria);

	/** 金額の再計算に必要な数量のみ取得する。対象伝票に属さない品目行や削除済みの行の場合はnullを返す。 */
	SalePriceVO read(@Param("saleId") Long saleId, @Param("saleLineId") Long saleLineId);

	/** 単価と金額を同時に更新する。金額はServiceで計算した値を受け取る。更新行数を返す。 */
	int updatePrice(SalePriceVO row);

	/** 品目行には更新日時がないため、伝票の更新日時を更新する。 */
	int updateSaleTimestamp(Long saleId);

}
