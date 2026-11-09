package hexa.erp.salesorder.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.salesorder.domain.SalesOrderCriteria;
import hexa.erp.salesorder.domain.SalesOrderLineVO;
import hexa.erp.salesorder.domain.SalesOrderVO;

public interface SalesOrderMapper {

	List<SalesOrderVO> getListWithPaging(SalesOrderCriteria criteria);

	int getTotalCount(SalesOrderCriteria criteria);

	// 受注伝票のヘッダー情報を取得する。
	SalesOrderVO read(Long salesOrderId);

	// 受注伝票の品目行を取得する。
	List<SalesOrderLineVO> getLines(Long salesOrderId);

	// 受注伝票のヘッダー情報を登録する。
	void insertSelectKey(SalesOrderVO saleOrder);

	// 受注伝票の品目行を登録する。
	void insertLineSelectKey(SalesOrderLineVO line);

	// 受注伝票のヘッダー情報を更新する。
	int update(SalesOrderVO salesOrder);

	// 受注伝票の品目行を更新する。
	int updateLine(SalesOrderLineVO line);

	// 受注伝票の品目行を削除する。
	int removeSelectedLines(@Param("salesOrderId") Long salesOrderId, @Param("lineIds") List<Long> lineIds);

	// 受注伝票のヘッダー情報を削除する。
	int remove(@Param("ids") List<Long> ids);

	// 受注伝票の品目行を削除する。
	int removeLines(@Param("ids") List<Long> ids);

	// 受注伝票の進行状態を変更する。
	int changeStatus(@Param("ids") List<Long> ids, @Param("progressStatus") String progressStatus);
}
