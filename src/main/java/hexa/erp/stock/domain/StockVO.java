package hexa.erp.stock.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class StockVO {

	// 在庫の識別情報
	private Long warehouseId;
	private Long itemId;

	// 現在庫情報
	private BigDecimal quantity;
	private String updatedAt;
}
