package hexa.erp.sale.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.sale.domain.SalePriceCriteria;
import hexa.erp.sale.domain.SalePriceVO;

/** 판매 단가 일괄변경. 삭제되지 않은 전표와 품목 행만 읽고 바꾼다. */
public interface SalePriceMapper {
	/** 검색 조건에 맞는 품목 행을 최신 일자순으로 한 페이지만 조회한다. */
	List<SalePriceVO> getListWithPaging(SalePriceCriteria criteria);
	
	/** 페이지 계산용. 검색 조건에 맞는 품목 행 전체 개수를 센다. */
	int getTotalCount(SalePriceCriteria criteria);
	
	/** 금액 재계산에 필요한 수량만 읽는다. 품목 행이 해당 전표에 속하지 않거나 삭제됐으면 null이다. */
	SalePriceVO read(@Param("saleId") Long saleId, @Param("saleLineId")Long saleLineId);
	
	/** 단가와 금액을 함께 바꾼다. 금액은 Service에서 계산해서 넘긴다. 바뀐 행 수를 돌려준다. */
	int updatePrice(SalePriceVO row);
	
	/** 품목 행에는 수정 시각이 없어서 전표의 수정 시각을 갱신한다. */
	int updateSaleTimestamp(Long saleId);

}
