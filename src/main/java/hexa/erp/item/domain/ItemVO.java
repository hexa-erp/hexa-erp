package hexa.erp.item.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ItemVO {

	// 品目の識別情報
	private Long itemId;
	private String itemCode;

	// 品目の基本情報
	private String itemName;
	private String specification;
	private String unit;
	private String itemType;

	// 品目の単価情報
	private BigDecimal inboundPrice;
	private BigDecimal outboundPrice;

	// 品目画像と摘要
	private String imagePath;
	private String note;

	// 利用状態と更新日時
	private String activeFlag;
	private String updatedAt;

	// 画面表示用の画像URL
	private String imageUrl;
}
