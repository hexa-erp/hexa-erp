package hexa.erp.stock.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.stock.domain.StockMovementVO;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.warehouse.domain.WarehouseLookupVO;

public interface StockMapper {

    // 전체 현재고 조회
    List<StockVO> getList();

    // 창고와 품목별 현재고 조회
    StockVO read(
        @Param("warehouseId") Long warehouseId,
        @Param("itemId") Long itemId
    );

    // 사용 중인 창고 조회
    List<WarehouseLookupVO> getWarehouseList();

    // 신규 현재고 등록
    void insert(StockVO stock);

    // 기존 현재고 수정
    int update(StockVO stock);

    // 재고 변동 이력 등록
    void insertMovement(StockMovementVO movement);
}
