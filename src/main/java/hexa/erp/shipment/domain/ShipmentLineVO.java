package hexa.erp.shipment.domain;

import java.math.BigDecimal;

import lombok.Data;

/** 出荷伝票の品目行。 */
@Data
public class ShipmentLineVO {
	private Long shipmentLineId;
	private Long shipmentId;
	// 出荷指示書から読み込んだ行には、元伝票の明細行IDを保持する。
	private Long shipInstructionLineId;
	private Long itemId;
	// 品目コードは取得時にITEMから読み込み、品目名は保存時の名前を明細行に保持する。
	private String itemCode;
	private String itemName;
	private String specification;
	private String unit;
	private BigDecimal quantity;
	private String note;
	private String deletedYn;
}
