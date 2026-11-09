package hexa.erp.warehouse.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.warehouse.domain.WarehouseCriteria;
import hexa.erp.warehouse.domain.WarehouseVO;

public interface WarehouseMapper {

	// 倉庫一覧を取得する。
	List<WarehouseVO> getListWithPaging(WarehouseCriteria criteria);

	// 検索条件に一致する総件数を取得する。
	int getTotalCount(WarehouseCriteria criteria);

	// 新規倉庫を登録する。
	void insertSelectKey(WarehouseVO warehouse);

	// 既存の倉庫情報を更新する。
	int update(WarehouseVO warehouse);

	// 倉庫の利用状態を変更する。
	int changeActive(@Param("ids") List<Long> ids, @Param("activeFlag") String activeFlag);
}
