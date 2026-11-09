package hexa.erp.warehouse.domain;

import lombok.Data;

@Data
public class WarehouseVO {

	// 倉庫の識別情報
	private Long warehouseId;
	private String warehouseCode;

	// 倉庫の基本情報
	private String warehouseName;
	private String warehouseType;

	// 利用状態と更新日時
	private String activeFlag;
	private String updatedAt;
}
