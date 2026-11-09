package hexa.erp.sale.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/** 単価一括変更のJSONリクエスト。changes配列で選択された行を保存する。 */
@Data
public class SalePriceFormVO {
	private List<SalePriceChangeVO> changes = new ArrayList<>();

}
