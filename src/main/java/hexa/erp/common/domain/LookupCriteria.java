package hexa.erp.common.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 선택 목록의 공통 검색·페이지 조건. 업무별 ID나 상태 조건은 넣지 않는다. */
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
