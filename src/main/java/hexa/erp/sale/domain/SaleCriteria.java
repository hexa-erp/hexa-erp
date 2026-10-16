package hexa.erp.sale.domain;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SaleCriteria {

    // 페이지 정보
    private int page = 1;
    private int pageSize = 25;

    // 검색 조건
    private String keyword = "";
    private String progressStatus = "";

    // 페이지 번호 보정
    public void setPage(int page) {
        this.page = Math.max(1, page);
    }

    // 페이지 크기 보정
    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(1, pageSize);
    }

    // 검색어 보정
    public void setKeyword(String keyword) {
        this.keyword =
            keyword == null
                ? ""
                : keyword.trim();
    }

    // 진행상태 보정
    public void setProgressStatus(
            String progressStatus) {

        this.progressStatus =
            progressStatus == null
                ? ""
                : progressStatus.trim();
    }
}
