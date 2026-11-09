package hexa.erp.warehouse.service;

import java.util.List;

import hexa.erp.warehouse.domain.WarehouseCriteria;
import hexa.erp.warehouse.domain.WarehouseVO;

public interface WarehouseService {

	// 倉庫一覧を取得する。
	List<WarehouseVO> getList(WarehouseCriteria criteria);

	// 検索条件に一致する総件数を取得する。
	int getTotal(WarehouseCriteria criteria);

	// 新規倉庫を登録する。
	void register(WarehouseVO warehouse);

	// 既存の倉庫情報を更新する。
	boolean modify(WarehouseVO warehouse);

	// 倉庫の利用状態を変更する。
	int changeActive(List<Long> ids, String activeFlag);
}
