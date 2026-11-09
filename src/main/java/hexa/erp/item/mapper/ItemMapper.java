package hexa.erp.item.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.item.domain.ItemCriteria;
import hexa.erp.item.domain.ItemVO;

public interface ItemMapper {

	// 品目一覧を取得する。
	List<ItemVO> getListWithPaging(ItemCriteria criteria);

	// 検索条件に一致する総件数を取得する。
	int getTotalCount(ItemCriteria criteria);

	// 品目を1件取得する。
	ItemVO read(Long itemId);

	// 新規品目を登録する。
	void insertSelectKey(ItemVO item);

	// 既存の品目情報を更新する。
	int update(ItemVO item);

	// 品目の利用状態を変更する。
	int changeActive(@Param("ids") List<Long> ids, @Param("activeFlag") String activeFlag);
}
