package hexa.erp.stock.service;

import java.util.List;

import hexa.erp.stock.domain.StockMovementVO;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.warehouse.domain.WarehouseLookupVO;

public interface StockService {

	// 現在庫の全件取得
	List<StockVO> getList();

	// 利用中の倉庫を取得する。
	List<WarehouseLookupVO> getWarehouseList();

	// 品目の現在庫を調整する。
	void adjust(StockVO stock);

	// 売上による在庫増減
	void applySaleMovement(StockMovementVO movement);
}
