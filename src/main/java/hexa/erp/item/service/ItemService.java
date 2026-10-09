package hexa.erp.item.service;

import java.util.List;

import hexa.erp.item.domain.ItemCriteria;
import hexa.erp.item.domain.ItemVO;

public interface ItemService {

    // 품목 목록 조회
    List<ItemVO> getList(ItemCriteria criteria);

    // 검색 조건 전체 건수 조회
    int getTotal(ItemCriteria criteria);

    // 품목 한 건 조회
    ItemVO get(Long itemId);

    // 신규 품목 등록
    void register(ItemVO item);

    // 기존 품목 정보 수정
    boolean modify(ItemVO item);

    // 품목 사용 여부 변경
    int changeActive(List<Long> ids, String activeFlag);
}
