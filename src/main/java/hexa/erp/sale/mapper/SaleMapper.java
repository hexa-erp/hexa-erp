package hexa.erp.sale.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.sale.domain.SaleCriteria;
import hexa.erp.sale.domain.SaleLineVO;
import hexa.erp.sale.domain.SaleVO;

public interface SaleMapper {

	// 売上一覧を取得する。
	List<SaleVO> getListWithPaging(SaleCriteria criteria);

	// 売上の総件数を取得する。
	int getTotalCount(SaleCriteria criteria);

	// 売上のヘッダー情報を取得する。
	SaleVO read(Long saleId);

	// 売上明細行を取得する。
	List<SaleLineVO> getLines(Long saleId);

	// 新規売上を登録する。
	void insertSelectKey(SaleVO sale);

	// 既存の売上を更新する。
	int update(SaleVO sale);

	// 新規売上明細行を登録する。
	void insertLineSelectKey(SaleLineVO line);

	// 既存の売上明細行を更新する。
	int updateLine(SaleLineVO line);

	// 選択した明細行を削除する。
	int removeSelectedLines(@Param("saleId") Long saleId, @Param("lineIds") List<Long> lineIds);

	// 選択した売上を削除する。
	int remove(@Param("ids") List<Long> ids);

	// 選択した売上明細行を削除する。
	int removeLines(@Param("ids") List<Long> ids);

	// 選択した売上の進行状態を変更する。
	int changeStatus(@Param("ids") List<Long> ids, @Param("progressStatus") String progressStatus);
}
