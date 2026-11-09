package hexa.erp.shipment.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/** 出荷伝票のヘッダーと品目行。 */
@Data
public class ShipmentVO {
	private Long shipmentId;
	private String shipmentNo;
	private String businessDate;
	// コードは取得時にマスターから読み込み、名前は保存時の値を伝票に保持する。
	private Long partnerId;
	private String partnerCode;
	private String partnerName;
	private Long warehouseId;
	private String warehouseCode;
	private String warehouseName;
	private Long assigneeId;
	private String assigneeCode;
	private String assigneeName;
	private String note;
	private String deliveryContact;
	private String deliveryPostalCode;
	private String deliveryAddress;
	private String progressStatus = "CONFIRMED";
	private String updatedAt;
	private String deletedYn;
	// 一覧表示用の計算値。
	private String itemSummary;
	private BigDecimal totalQuantity;
	private List<ShipmentLineVO> lines = new ArrayList<>();
	// 更新画面で削除指定した既存品目行のID。
	private List<Long> removedLineIds = new ArrayList<>();

}
