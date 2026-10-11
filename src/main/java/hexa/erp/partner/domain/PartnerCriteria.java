package hexa.erp.partner.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PartnerCriteria {

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

    public void setKeyword(String keyword) {
        this.keyword = keyword == null ? "" : keyword.trim();
    }

    public void setIncludeInactive(String includeInactive) {
        this.includeInactive = "Y".equals(includeInactive) ? "Y" : "N";
    }
}
