package hexa.erp.stock.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class StockVO {

    // 재고 식별 정보
    private Long warehouseId;
    private Long itemId;

    // 현재 재고 정보
    private BigDecimal quantity;
    private String updatedAt;
}
