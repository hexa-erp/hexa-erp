package hexa.erp.item.service;

import java.util.List;

import hexa.erp.item.domain.ItemCriteria;
import hexa.erp.item.domain.ItemVO;

public interface ItemService {

	// 品目一覧を取得する。
	List<ItemVO> getList(ItemCriteria criteria);

	// 検索条件に一致する総件数を取得する。
	int getTotal(ItemCriteria criteria);

	// 品目を1件取得する。
	ItemVO get(Long itemId);

	// 新規品目を登録する。
	void register(ItemVO item);

	// 既存の品目情報を更新する。
	boolean modify(ItemVO item);

	// 品目の利用状態を変更する。
	int changeActive(List<Long> ids, String activeFlag);
}
