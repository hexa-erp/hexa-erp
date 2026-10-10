package hexa.erp.stock.service;

import java.util.List;

import hexa.erp.stock.domain.StockVO;
import hexa.erp.warehouse.domain.WarehouseLookupVO;

public interface StockService {

    // 전체 현재고 조회
    List<StockVO> getList();

    // 사용 중인 창고 조회
    List<WarehouseLookupVO> getWarehouseList();

    // 품목 현재고 조정
    void adjust(StockVO stock);
}
