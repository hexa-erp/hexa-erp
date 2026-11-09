package hexa.erp.shipment.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 出荷一覧の検索条件とページ情報。不正な値は既定値に補正する。 */
@Getter
@Setter
@ToString
public class ShipmentCriteria {
	// 初期表示は1ページ目・25件とし、進行状態を指定しない場合は全件を対象とする。
	private int page = 1;
	private int pageSize = 25;
	private String keyword = "";
	private String progressStatus = "";

	public void setPage(int page) {
		this.page = Math.max(1, page);
	}

	public void setPageSize(int pageSize) {
		this.pageSize = Math.max(1, pageSize);
	}

	// 検索語の前後の空白を除去してから品目名の部分一致検索に使用する。
	public void setKeyword(String keyword) {
		this.keyword = keyword == null ? "" : keyword.trim();
	}

	public void setProgressStatus(String progressStatus) {
		this.progressStatus = progressStatus == null ? "" : progressStatus.trim();
	}

}
