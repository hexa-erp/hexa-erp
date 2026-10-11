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

    // 창고 Mapper 연결
    private final WarehouseMapper mapper;

    // 창고 목록 조회
    @Override
    public List<WarehouseVO> getList(WarehouseCriteria criteria) {
        return mapper.getListWithPaging(criteria);
    }

    // 검색 조건 전체 건수 조회
    @Override
    public int getTotal(WarehouseCriteria criteria) {
        return mapper.getTotalCount(criteria);
    }

    // 신규 창고 등록
    @Override
    public void register(WarehouseVO warehouse) {
        log.info("창고 등록 처리");
        mapper.insertSelectKey(warehouse);
    }

    // 기존 창고 정보 수정
    @Override
    public boolean modify(WarehouseVO warehouse) {
        log.info("창고 수정: " + warehouse.getWarehouseId());
        return mapper.update(warehouse) == 1;
    }

    // 창고 사용 여부 변경
    @Override
    public int changeActive(List<Long> ids, String activeFlag) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        if (!"Y".equals(activeFlag) && !"N".equals(activeFlag)) {
            throw new IllegalArgumentException("사용 여부는 Y 또는 N이어야 합니다.");
        }

        log.info("창고 사용 여부 변경: " + activeFlag);
        return mapper.changeActive(ids, activeFlag);
    }
}
