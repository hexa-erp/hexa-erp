package hexa.erp.stock.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.stock.domain.StockMovementVO;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.warehouse.domain.WarehouseLookupVO;

public interface StockMapper {

	// 現在庫の全件取得
	List<StockVO> getList();

	// 倉庫・品目別の現在庫を取得する。
	StockVO read(@Param("warehouseId") Long warehouseId, @Param("itemId") Long itemId);

	// 利用中の倉庫を取得する。
	List<WarehouseLookupVO> getWarehouseList();

	// 新規現在庫を登録する。
	void insert(StockVO stock);

	// 既存の現在庫を更新する。
	int update(StockVO stock);

	// 現在庫に増減数量を反映する。
	int addQuantity(StockMovementVO movement);

	// 在庫変動履歴を登録する。
	void insertMovement(StockMovementVO movement);
}
