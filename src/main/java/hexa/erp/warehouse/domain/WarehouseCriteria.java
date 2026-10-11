package hexa.erp.warehouse.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class WarehouseCriteria {

    // 기본 조회 조건
    private int page = 1;
    private int pageSize = 50;
    private String keyword = "";
    private String includeInactive = "N";

    // 페이지 번호 보정
    public void setPage(int page) {
        this.page = Math.max(1, page);
    }

    // 페이지 크기 보정
    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(1, pageSize);
    }

    // 검색어 공백 보정
    public void setKeyword(String keyword) {
        this.keyword = keyword == null ? "" : keyword.trim();
    }

    // 사용중단 포함 값 보정
    public void setIncludeInactive(String includeInactive) {
        this.includeInactive = "Y".equals(includeInactive) ? "Y" : "N";
    }
}
