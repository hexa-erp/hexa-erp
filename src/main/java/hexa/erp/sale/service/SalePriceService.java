package hexa.erp.sale.service;

import java.util.List;

import hexa.erp.sale.domain.SalePriceChangeVO;
import hexa.erp.sale.domain.SalePriceCriteria;
import hexa.erp.sale.domain.SalePriceVO;

/** 판매 단가 일괄변경 업무 로직. */
public interface SalePriceService {
	/** 검색 조건에 맞는 품목 행을 한 페이지만 조회한다. */
	List<SalePriceVO> getList(SalePriceCriteria criteria);

	/** 검색 조건에 맞는 품목 행 전체 개수. 페이지 계산에 쓴다. */
	int getTotal(SalePriceCriteria criteria);

	/**
	 * 선택한 품목 행의 단가를 바꾸고 공급가액·부가세·합계를 다시 계산해 저장한다.
	 * 한 건이라도 실패하면 전부 취소하고 IllegalArgumentException을 던진다. 변경한 건수를 반환한다.
	 */
	int savePrices(List<SalePriceChangeVO> changes);

}
