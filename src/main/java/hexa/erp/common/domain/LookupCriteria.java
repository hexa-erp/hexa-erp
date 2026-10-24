package hexa.erp.common.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 選択画面で使用する検索条件とページ情報。 */
@Getter
@Setter
@ToString
public class LookupCriteria {
	private int pageNum = 1;
	private int amount = 25;
	private String keyword = "";

	public void setPageNum(int pageNum) {
		this.pageNum = Math.max(1, pageNum);
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword == null ? "" : keyword.trim();
	}
}
