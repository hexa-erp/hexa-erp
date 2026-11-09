package hexa.erp.stock.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class StockMovementVO {

	// 在庫変動の識別情報
	private Long movementId;
	private Long saleId;
	private Long saleLineId;

	// 在庫の対象情報
	private Long warehouseId;
	private Long itemId;

	// 在庫増減の情報
	private BigDecimal quantityDelta;
	private String occurredAt;
}
