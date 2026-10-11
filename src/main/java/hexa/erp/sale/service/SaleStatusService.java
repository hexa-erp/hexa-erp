package hexa.erp.sale.service;

import hexa.erp.sale.domain.SaleStatusCriteria;
import hexa.erp.sale.domain.SaleStatusVO;

/** 판매 현황 업무 로직. */
public interface SaleStatusService {
	/** 검색 결과를 월별로 묶고 합계를 계산해 반환한다. */
	SaleStatusVO getStatus(SaleStatusCriteria criteria);

}
