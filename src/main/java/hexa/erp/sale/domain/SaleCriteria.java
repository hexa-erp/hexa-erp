package hexa.erp.sale.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SaleCriteria {

	// ページ情報
	private int page = 1;
	private int pageSize = 25;

	// 検索条件
	private String keyword = "";
	private String progressStatus = "";

	// ページ番号を補正する。
	public void setPage(int page) {
		this.page = Math.max(1, page);
	}

	// ページサイズを補正する。
	public void setPageSize(int pageSize) {
		this.pageSize = Math.max(1, pageSize);
	}

	// 検索語を補正する。
	public void setKeyword(String keyword) {
		this.keyword = keyword == null ? "" : keyword.trim();
	}

	// 進行状態を補正する。
	public void setProgressStatus(String progressStatus) {

		this.progressStatus = progressStatus == null ? "" : progressStatus.trim();
	}
}
