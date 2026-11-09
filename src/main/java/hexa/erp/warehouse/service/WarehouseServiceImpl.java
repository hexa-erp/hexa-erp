package hexa.erp.warehouse.service;

import java.util.List;

import org.springframework.stereotype.Service;

import hexa.erp.warehouse.domain.WarehouseCriteria;
import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

	// 倉庫Mapperの参照
	private final WarehouseMapper mapper;

	// 倉庫一覧を取得する。
	@Override
	public List<WarehouseVO> getList(WarehouseCriteria criteria) {
		return mapper.getListWithPaging(criteria);
	}

	// 検索条件に一致する総件数を取得する。
	@Override
	public int getTotal(WarehouseCriteria criteria) {
		return mapper.getTotalCount(criteria);
	}

	// 新規倉庫を登録する。
	@Override
	public void register(WarehouseVO warehouse) {
		log.info("倉庫の登録処理");
		mapper.insertSelectKey(warehouse);
	}

	// 既存の倉庫情報を更新する。
	@Override
	public boolean modify(WarehouseVO warehouse) {
		log.info("倉庫の更新: " + warehouse.getWarehouseId());
		return mapper.update(warehouse) == 1;
	}

	// 倉庫の利用状態を変更する。
	@Override
	public int changeActive(List<Long> ids, String activeFlag) {
		if (ids == null || ids.isEmpty()) {
			return 0;
		}

		if (!"Y".equals(activeFlag) && !"N".equals(activeFlag)) {
			throw new IllegalArgumentException("利用状態にはYまたはNを指定してください。");
		}

		log.info("倉庫の利用状態変更: " + activeFlag);
		return mapper.changeActive(ids, activeFlag);
	}
}
