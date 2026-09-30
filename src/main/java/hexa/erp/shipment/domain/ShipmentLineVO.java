package hexa.erp.shipment.domain;

import java.math.BigDecimal;

import lombok.Data;

/** 출하 전표의 품목 행. */
@Data
public class ShipmentLineVO {
	private Long shipmentLineId;
	private Long shipmentId;
	// 출하지시서에서 불러온 행이면 원전표 상세행 ID를 가진다.
	private Long shipInstructionLineId;
	private Long itemId;
	// 품목코드는 조회 시 ITEM에서 가져오며, 품목명은 저장 시점 이름을 행에 보관한다.
	private String itemCode;
	private String itemName;
	private String specification;
	private String unit;
	private BigDecimal quantity;
	private String note;
	private String deletedYn;
}
