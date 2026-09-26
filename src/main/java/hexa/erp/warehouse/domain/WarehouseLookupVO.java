package hexa.erp.warehouse.domain;

import lombok.Data;

@Data
public class WarehouseLookupVO {
	private Long warehouseId;
	private String warehouseCode;
	private String warehouseName;
	private String warehouseType;
	private String activeFlag;
}
