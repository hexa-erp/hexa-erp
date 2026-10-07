package hexa.erp.quotation.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.quotation.domain.QuotationCriteria;
import hexa.erp.quotation.domain.QuotationLineVO;
import hexa.erp.quotation.domain.QuotationVO;

public interface QuotationMapper {

	//견적서 기본 정보 조회
	public QuotationVO read(Long quotationId);
	
	//견적서 품목 행 목록 조회
	public List<QuotationLineVO> getLines(Long quotationId);
	
	public int getTotalCount(QuotationCriteria criteria);
	
	public List<QuotationVO> getListWithPaging(QuotationCriteria criteria);
	
	//견적서 기본 정보 입력
	public void insertSelectKey(QuotationVO quotation);
	
	//견적서 품목 행 입력
	public void insertLineSelectKey (QuotationLineVO line);
	
	//견적서 기본 정보 수정
	public int update(QuotationVO quotation);
	
	//견적서 품목 행 수정
	public int updateLine(QuotationLineVO line);
	
	//견적서 삭제 - 기본 정보
	public int remove(@Param("ids") List<Long> ids);
	
	//견저서 삭제 - 품목 행
	public int removeLines(@Param("ids") List<Long> ids);
	
	//품목행만  삭제
	public int removeSelectedLines(@Param("quotationId") Long quotationId, @Param("lineIds") List<Long> lineIds);
	
	//진행 상태 변경
	public int changeStatus(@Param("ids") List<Long> ids, @Param("progressStatus") String progressStatus);
}
