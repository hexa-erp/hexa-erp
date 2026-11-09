package hexa.erp.sale.service;

import java.util.List;

import hexa.erp.sale.domain.SaleCriteria;
import hexa.erp.sale.domain.SaleVO;

public interface SaleService {

	// 売上一覧を取得する。
	List<SaleVO> getList(SaleCriteria criteria);

	// 売上の総件数を取得する。
	int getTotal(SaleCriteria criteria);

	// 売上を1件取得する。
	SaleVO get(Long saleId);

	// 売上を保存する。
	Long save(SaleVO sale);

	// 選択した売上を削除する。
	int remove(List<Long> selectedIds);

	// 選択した売上の進行状態を変更する。
	int changeStatus(List<Long> selectedIds, String progressStatus);
}
