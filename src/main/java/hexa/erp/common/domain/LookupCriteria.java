package hexa.erp.common.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 선택창에서 사용하는 검색·페이지 조건. */
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
