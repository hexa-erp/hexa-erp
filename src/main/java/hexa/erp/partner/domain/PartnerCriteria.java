package hexa.erp.partner.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PartnerCriteria {

	// 初期表示は1ページ目（50件）とし、「使用停止を含む」は未選択で開始する。
	private int page = 1;
	private int pageSize = 50;
	private String keyword = "";
	private String includeInactive = "N";

	public void setPage(int page) {
		this.page = Math.max(1, page);
	}

	public void setPageSize(int pageSize) {
		this.pageSize = Math.max(1, pageSize);
	}

	// 検索語の前後の空白を除去する。
	public void setKeyword(String keyword) {
		this.keyword = keyword == null ? "" : keyword.trim();
	}

	public void setIncludeInactive(String includeInactive) {
		this.includeInactive = "Y".equals(includeInactive) ? "Y" : "N";
	}
}
