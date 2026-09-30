package hexa.erp.shipment.service;

import java.util.List;

import hexa.erp.shipment.domain.ShipmentCriteria;
import hexa.erp.shipment.domain.ShipmentVO;

/** 출하 전표 업무 로직. */
public interface ShipmentService {
	List<ShipmentVO> getList(ShipmentCriteria criteria);

	int getTotal(ShipmentCriteria criteria);

	/** 전표와 품목 행을 함께 조회한다. 없거나 삭제된 전표는 null이다. */
	ShipmentVO get(Long shipmentId);

	/** shipmentId가 없으면 등록, 있으면 수정한다. 입력값 오류는 IllegalArgumentException으로 알린다. */
	Long save(ShipmentVO shipment);

	/** 선택한 전표와 품목 행을 삭제 표시하고, 삭제된 전표 수를 반환한다. */
	int remove(List<Long> selectedIds);

	/** 진행상태는 CONFIRMED, UNCONFIRMED만 허용한다. */
	int changeStatus(List<Long> selectedIds, String progressStatus);

}
