package hexa.erp.shipinstruction.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ShipInstructionCriteria {
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