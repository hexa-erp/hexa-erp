package hexa.erp.sale.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/** 売上集計の検索条件。空の条件は検索に使用しない。 */
@Data
public class SaleStatusCriteria {
	// 日付形式はYYYY-MM-DDとし、終了日当日まで含める。
	private String startDate;
	private String endDate;
	private String keyword;
	private String progressStatus;
	private String specification;
	// 数量・金額の範囲条件。min以上、max以下で比較する。
	private BigDecimal minQuantity;
	private BigDecimal maxQuantity;
	private BigDecimal minUnitPrice;
	private BigDecimal maxUnitPrice;
	private BigDecimal minSupplyAmount;
	private BigDecimal maxSupplyAmount;
	private BigDecimal minVatAmount;
	private BigDecimal maxVatAmount;
	// フィルター選択画面で選んだID一覧。同じ一覧内ではOR条件で検索する。
	private List<String> warehouseIds = new ArrayList<>();
	private List<String> partnerIds = new ArrayList<>();
	private List<String> itemIds = new ArrayList<>();
	private List<String> assigneeIds = new ArrayList<>();

}
