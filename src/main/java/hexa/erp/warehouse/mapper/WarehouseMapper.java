package hexa.erp.warehouse.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.warehouse.domain.WarehouseCriteria;
import hexa.erp.warehouse.domain.WarehouseVO;

public interface WarehouseMapper {

    // 창고 목록 조회
    List<WarehouseVO> getListWithPaging(WarehouseCriteria criteria);

    // 검색 조건 전체 건수 조회
    int getTotalCount(WarehouseCriteria criteria);

    // 신규 창고 등록
    void insertSelectKey(WarehouseVO warehouse);

    // 기존 창고 정보 수정
    int update(WarehouseVO warehouse);

    // 창고 사용 여부 변경
    int changeActive(
        @Param("ids") List<Long> ids,
        @Param("activeFlag") String activeFlag
    );
}
