package hexa.erp.sale.mapper;

import java.util.List;

import hexa.erp.sale.domain.SaleStatusCriteria;
import hexa.erp.sale.domain.SaleStatusRowVO;

/** 판매 현황 조회. 삭제되지 않은 전표와 품목 행만 읽는다. */
public interface SaleStatusMapper {
	/** 검색 조건에 맞는 품목 행 전체를 일자순으로 조회한다. 페이지를 나누지 않는다. */
	List<SaleStatusRowVO> getStatusRows(SaleStatusCriteria criteria);

}
