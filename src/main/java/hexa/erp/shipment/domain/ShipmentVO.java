package hexa.erp.shipment.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/** 출하 전표 헤더와 품목 행. */
@Data
public class ShipmentVO {
	private Long shipmentId;
	private String shipmentNo;
	private String businessDate;
	// 코드는 조회 시 마스터에서 가져오며, 이름은 저장 시점 값을 전표에 보관한다.
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
	// 목록 표시용 계산값이다.
	private String itemSummary;
	private BigDecimal totalQuantity;
	private List<ShipmentLineVO> lines = new ArrayList<>();
	// 수정 화면에서 삭제한 기존 품목 행 ID다.
	private List<Long> removedLineIds = new ArrayList<>();

}
