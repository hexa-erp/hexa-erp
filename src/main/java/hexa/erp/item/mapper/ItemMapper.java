package hexa.erp.item.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.item.domain.ItemCriteria;
import hexa.erp.item.domain.ItemVO;

public interface ItemMapper {

    // 품목 목록 조회
    List<ItemVO> getListWithPaging(ItemCriteria criteria);

    // 검색 조건 전체 건수 조회
    int getTotalCount(ItemCriteria criteria);

    // 품목 한 건 조회
    ItemVO read(Long itemId);

    // 신규 품목 등록
    void insertSelectKey(ItemVO item);

    // 기존 품목 정보 수정
    int update(ItemVO item);

    // 품목 사용 여부 변경
    int changeActive(
        @Param("ids") List<Long> ids,
        @Param("activeFlag") String activeFlag
    );
}
