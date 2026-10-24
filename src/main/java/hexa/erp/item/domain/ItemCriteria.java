package hexa.erp.item.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ItemCriteria {

	// 既定の検索条件
	private int page = 1;
	private int pageSize = 50;
	private String keyword = "";
	private String includeInactive = "N";

	// ページ番号を補正する。
	public void setPage(int page) {
		this.page = Math.max(1, page);
	}

	// ページサイズを補正する。
	public void setPageSize(int pageSize) {
		this.pageSize = Math.max(1, pageSize);
	}

	// 検索語の前後の空白を除去する。
	public void setKeyword(String keyword) {
		this.keyword = keyword == null ? "" : keyword.trim();
	}

	// 利用停止データを含めるかどうかの値を補正する。
	public void setIncludeInactive(String includeInactive) {
		this.includeInactive = "Y".equals(includeInactive) ? "Y" : "N";
	}
}
