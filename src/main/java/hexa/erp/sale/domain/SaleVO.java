package hexa.erp.sale.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SaleVO {

    // 판매 식별 정보
    private Long saleId;
    private String saleNo;
    private String businessDate;

    // 거래처 정보
    private Long partnerId;
    private String partnerCode;
    private String partnerName;

    // 출하창고 정보
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;

    // 담당자 정보
    private Long assigneeId;
    private String assigneeCode;
    private String assigneeName;

    // 판매 상태 정보
    private String note;
    private String progressStatus = "CONFIRMED";
    private String createdAt;
    private String updatedAt;
    private String deletedYn;

    // 판매 목록 표시 정보
    private String itemSummary;
    private BigDecimal totalAmount;

    // 판매 상세 정보
    private List<SaleLineVO> lines =
        new ArrayList<>();

    private List<Long> removedLineIds =
        new ArrayList<>();
}
