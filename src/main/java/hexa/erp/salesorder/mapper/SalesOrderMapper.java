package hexa.erp.salesorder.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.salesorder.domain.SalesOrderCriteria;
import hexa.erp.salesorder.domain.SalesOrderLineVO;
import hexa.erp.salesorder.domain.SalesOrderVO;

public interface SalesOrderMapper {
	
	List<SalesOrderVO> getListWithPaging(SalesOrderCriteria criteria);
	 
    int getTotalCount(SalesOrderCriteria criteria);
    
	//주문서 기본 정보 조회
	SalesOrderVO read(Long salesOrderId);
	
	//주문서 품목 행 조회
	List<SalesOrderLineVO> getLines(Long salesOrderId);
	
	//주문서 기본 정보 입력
	void insertSelectKey(SalesOrderVO saleOrder);
	
	//주문서 품목 행 입력
	void insertLineSelectKey(SalesOrderLineVO line);
	
	//주문서 기본 정보 수정
	int update(SalesOrderVO salesOrder);
	
	//주문서 품목 행 수정
	int updateLine(SalesOrderLineVO line);
	
	//주문서 품목 행 삭제
	int removeSelectedLines(@Param("salesOrderId") Long salesOrderId, @Param("lineIds") List<Long> lineIds);
	
	//주문서 삭제 - 기본 정보
	int remove (@Param("ids") List<Long> ids);
	
	//주문서 삭제 - 품목 행
	int removeLines(@Param("lineIds") List<Long> lineIds);
	
	//주문서 진행 상태 변경
	int changeStatus(@Param("ids") List<Long> ids, @Param("progressStatus") String progressStatus);
}
