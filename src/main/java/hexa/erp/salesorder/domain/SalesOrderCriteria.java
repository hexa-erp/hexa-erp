package hexa.erp.salesorder.domain;

import lombok.Data;

@Data
public class SalesOrderCriteria {
	
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
