package hexa.erp.sale.service;

import hexa.erp.sale.domain.SaleVO;

public interface SaleService {

    // 판매 한 건 조회
    SaleVO get(Long saleId);

    // 판매 저장
    Long save(SaleVO sale);
}
