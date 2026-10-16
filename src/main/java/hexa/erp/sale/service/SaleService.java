package hexa.erp.sale.service;

import java.util.List;

import hexa.erp.sale.domain.SaleCriteria;
import hexa.erp.sale.domain.SaleVO;

public interface SaleService {

    // 판매 목록 조회
    List<SaleVO> getList(
        SaleCriteria criteria
    );

    // 판매 전체 건수 조회
    int getTotal(
        SaleCriteria criteria
    );

    // 판매 한 건 조회
    SaleVO get(Long saleId);

    // 판매 저장
    Long save(SaleVO sale);

    // 선택한 판매 삭제
    int remove(List<Long> selectedIds);

    // 선택한 판매 진행상태 변경
    int changeStatus(
        List<Long> selectedIds,
        String progressStatus
    );
}
