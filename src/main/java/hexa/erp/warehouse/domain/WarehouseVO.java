package hexa.erp.warehouse.domain;

import lombok.Data;

@Data
public class WarehouseVO {

    // 창고 식별 정보
    private Long warehouseId;
    private String warehouseCode;

    // 창고 기본 정보
    private String warehouseName;
    private String warehouseType;

    // 사용 여부와 변경 시각
    private String activeFlag;
    private String updatedAt;
}
