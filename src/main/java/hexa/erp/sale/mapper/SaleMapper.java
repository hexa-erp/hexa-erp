package hexa.erp.sale.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.sale.domain.SaleLineVO;
import hexa.erp.sale.domain.SaleVO;

public interface SaleMapper {

    // 판매 기본정보 조회
    SaleVO read(Long saleId);

    // 판매 상세행 조회
    List<SaleLineVO> getLines(Long saleId);

    // 신규 판매 등록
    void insertSelectKey(SaleVO sale);

    // 기존 판매 수정
    int update(SaleVO sale);

    // 신규 판매 상세행 등록
    void insertLineSelectKey(SaleLineVO line);

    // 기존 판매 상세행 수정
    int updateLine(SaleLineVO line);

    // 선택한 상세행 삭제 처리
    int removeSelectedLines(
        @Param("saleId") Long saleId,
        @Param("lineIds") List<Long> lineIds
    );
}
