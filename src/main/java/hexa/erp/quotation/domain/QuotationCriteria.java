package hexa.erp.quotation.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QuotationCriteria {
	
	private int page = 1;
	private int pageSize = 25;
	
	private String type;
	private String keyword;
	
	public void setPage(int page) {
		this.page = Math.max(1, page);
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword == null? "": keyword.trim();
	}
}
