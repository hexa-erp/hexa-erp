package hexa.erp.shipment.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 出荷一覧の検索条件とページ情報。不正な値は既定値に補正する。 */
@Getter
@Setter
@ToString
public class ShipmentCriteria {
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

	public void setKeyword(String keyword) {
		this.keyword = keyword == null ? "" : keyword.trim();
	}

	public void setProgressStatus(String progressStatus) {
		this.progressStatus = progressStatus == null ? "" : progressStatus.trim();
	}

}
