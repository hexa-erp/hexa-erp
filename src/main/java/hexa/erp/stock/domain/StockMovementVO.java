package hexa.erp.stock.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class StockMovementVO {

    // 재고 변동 식별 정보
    private Long movementId;
    private Long saleId;
    private Long saleLineId;

    // 재고 대상 정보
    private Long warehouseId;
    private Long itemId;

    // 재고 증감 정보
    private BigDecimal quantityDelta;
    private String occurredAt;
}
