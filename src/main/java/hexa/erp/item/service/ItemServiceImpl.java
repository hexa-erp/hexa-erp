package hexa.erp.item.service;

import java.util.List;

import org.springframework.stereotype.Service;

import hexa.erp.item.domain.ItemCriteria;
import hexa.erp.item.domain.ItemVO;
import hexa.erp.item.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

	// 品目Mapperの参照
	private final ItemMapper mapper;

	// 品目一覧を取得する。
	@Override
	public List<ItemVO> getList(ItemCriteria criteria) {
		return mapper.getListWithPaging(criteria);
	}

	// 検索条件に一致する総件数を取得する。
	@Override
	public int getTotal(ItemCriteria criteria) {
		return mapper.getTotalCount(criteria);
	}

	// 品目を1件取得する。
	@Override
	public ItemVO get(Long itemId) {
		return mapper.read(itemId);
	}

	// 新規品目を登録する。
	@Override
	public void register(ItemVO item) {
		log.info("品目の登録処理");
		mapper.insertSelectKey(item);
	}

	// 既存の品目情報を更新する。
	@Override
	public boolean modify(ItemVO item) {
		log.info("品目の更新: " + item.getItemId());
		return mapper.update(item) == 1;
	}

	// 品目の利用状態を変更する。
	@Override
	public int changeActive(List<Long> ids, String activeFlag) {

		if (ids == null || ids.isEmpty()) {
			return 0;
		}

		if (!"Y".equals(activeFlag) && !"N".equals(activeFlag)) {
			throw new IllegalArgumentException("利用状態にはYまたはNを指定してください。");
		}

		log.info("品目の利用状態変更: " + activeFlag);
		return mapper.changeActive(ids, activeFlag);
	}
}
