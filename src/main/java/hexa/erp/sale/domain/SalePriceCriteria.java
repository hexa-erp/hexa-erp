package hexa.erp.sale.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 판매 단가 일괄변경 화면의 검색 조건. 값이 비어 있는 조건은 검색에 적용하지 않는다. */
@Getter
@Setter
@ToString
public class SalePriceCriteria {
	// 페이지 번호는 1부터 시작한다.
	private int page = 1;
	private int pageSize = 25;
	// 날짜는 YYYY-MM-DD 형식이며, 종료일 당일까지 포함한다.
	private String startDate;
	private String endDate;
	private String keyword = "";
	private String progressStatus = "";
	// 필터 선택창에서 고른 ID 목록. 같은 목록 안에서는 OR로 검색한다.
	private List<String> warehouseIds = new ArrayList<>();
	private List<String> partnerIds = new ArrayList<>();
	private List<String> itemIds = new ArrayList<>();

	// 1보다 작은 값이 들어오면 1로 맞춘다. (pageSize도 동일)
	public void setPage(int page) {
		this.page = Math.max(1, page);
	}

	public void setPageSize(int pageSize) {
		this.pageSize = Math.max(1, pageSize);
	}

	// null은 빈 문자열로 바꾸고 앞뒤 공백을 제거한다. (progressStatus도 동일)
	public void setKeyword(String keyword) {
		this.keyword = keyword == null ? "" : keyword.trim();
	}

	public void setProgressStatus(String progressStatus) {
		this.progressStatus = progressStatus == null ? "" : progressStatus.trim();
	}

}
