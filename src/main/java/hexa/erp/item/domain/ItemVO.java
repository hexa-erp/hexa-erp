package hexa.erp.item.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ItemVO {

    // 품목 식별 정보
    private Long itemId;
    private String itemCode;

    // 품목 기본 정보
    private String itemName;
    private String specification;
    private String unit;
    private String itemType;

    // 품목 단가 정보
    private BigDecimal inboundPrice;
    private BigDecimal outboundPrice;

    // 품목 이미지와 적요
    private String imagePath;
    private String note;

    // 사용 여부와 변경 시각
    private String activeFlag;
    private String updatedAt;

    // 화면 표시용 이미지 주소
    private String imageUrl;
}
