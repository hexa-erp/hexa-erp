package hexa.erp.shipment.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 출하 목록의 검색·페이지 조건. 잘못된 값은 기본값으로 보정한다. */
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
