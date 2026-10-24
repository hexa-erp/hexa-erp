package hexa.erp.sale.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

/** 売上集計画面に渡す結果。 */
@Data
public class SaleStatusVO {
	// 月別にまとめた行の一覧
	private List<Map<String, Object>> monthGroups = new ArrayList<>();
	// 検索結果全体の数量・金額合計（項目名 → 合計）
	private Map<String, BigDecimal> totals = new LinkedHashMap<>();

}
