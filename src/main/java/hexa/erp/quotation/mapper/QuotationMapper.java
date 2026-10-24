package hexa.erp.quotation.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.quotation.domain.QuotationCriteria;
import hexa.erp.quotation.domain.QuotationLineVO;
import hexa.erp.quotation.domain.QuotationVO;

public interface QuotationMapper {

	// 見積書のヘッダー情報を取得する。
	public QuotationVO read(Long quotationId);

	// 見積書の品目行一覧を取得する。
	public List<QuotationLineVO> getLines(Long quotationId);

	public int getTotalCount(QuotationCriteria criteria);

	public List<QuotationVO> getListWithPaging(QuotationCriteria criteria);

	// 見積書のヘッダー情報を登録する。
	public void insertSelectKey(QuotationVO quotation);

	// 見積書の品目行を登録する。
	public void insertLineSelectKey(QuotationLineVO line);

	// 見積書のヘッダー情報を更新する。
	public int update(QuotationVO quotation);

	// 見積書の品目行を更新する。
	public int updateLine(QuotationLineVO line);

	// 見積書のヘッダー情報を削除する。
	public int remove(@Param("ids") List<Long> ids);

	// 見積書の品目行を削除する。
	public int removeLines(@Param("ids") List<Long> ids);

	// 品目行のみ削除する。
	public int removeSelectedLines(@Param("quotationId") Long quotationId, @Param("lineIds") List<Long> lineIds);

	// 進行状態を変更する。
	public int changeStatus(@Param("ids") List<Long> ids, @Param("progressStatus") String progressStatus);
}
