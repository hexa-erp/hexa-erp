package hexa.erp.warehouse.service;

import java.util.List;

import hexa.erp.warehouse.domain.WarehouseCriteria;
import hexa.erp.warehouse.domain.WarehouseVO;

public interface WarehouseService {

    // 창고 목록 조회
    List<WarehouseVO> getList(WarehouseCriteria criteria);

    // 검색 조건 전체 건수 조회
    int getTotal(WarehouseCriteria criteria);

    // 신규 창고 등록
    void register(WarehouseVO warehouse);

    // 기존 창고 정보 수정
    boolean modify(WarehouseVO warehouse);

    // 창고 사용 여부 변경
    int changeActive(List<Long> ids, String activeFlag);
}
