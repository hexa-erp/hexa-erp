package hexa.erp.shipment.service;

import java.util.List;

import hexa.erp.shipment.domain.ShipmentCriteria;
import hexa.erp.shipment.domain.ShipmentVO;

/** 出荷伝票の業務ロジック。 */
public interface ShipmentService {
	List<ShipmentVO> getList(ShipmentCriteria criteria);

	int getTotal(ShipmentCriteria criteria);

	/** 伝票と品目行をまとめて取得する。伝票が存在しない場合や削除済みの場合はnullを返す。 */
	ShipmentVO get(Long shipmentId);

	/** shipmentIdがなければ新規登録、あれば更新する。入力エラーはIllegalArgumentExceptionで通知する。 */
	Long save(ShipmentVO shipment);

	/** 選択した伝票と品目行を論理削除し、削除した伝票数を返す。 */
	int remove(List<Long> selectedIds);

	/** 進行状態にはCONFIRMED、UNCONFIRMEDのみ許可する。 */
	int changeStatus(List<Long> selectedIds, String progressStatus);

}
