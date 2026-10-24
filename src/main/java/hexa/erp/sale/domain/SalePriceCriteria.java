package hexa.erp.sale.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 売上単価一括変更画面の検索条件。空の条件は検索に使用しない。 */
@Getter
@Setter
@ToString
public class SalePriceCriteria {
	// ページ番号は1から始まる。
	private int page = 1;
	private int pageSize = 25;
	// 日付形式はYYYY-MM-DDとし、終了日当日まで含める。
	private String startDate;
	private String endDate;
	private String keyword = "";
	private String progressStatus = "";
	// フィルター選択画面で選んだID一覧。同じ一覧内ではOR条件で検索する。
	private List<String> warehouseIds = new ArrayList<>();
	private List<String> partnerIds = new ArrayList<>();
	private List<String> itemIds = new ArrayList<>();

	// 1未満の値は1に補正する。pageSizeも同様。
	public void setPage(int page) {
		this.page = Math.max(1, page);
	}

	public void setPageSize(int pageSize) {
		this.pageSize = Math.max(1, pageSize);
	}

	// nullを空文字に置き換え、前後の空白を除去する。progressStatusも同様。
	public void setKeyword(String keyword) {
		this.keyword = keyword == null ? "" : keyword.trim();
	}

	public void setProgressStatus(String progressStatus) {
		this.progressStatus = progressStatus == null ? "" : progressStatus.trim();
	}

}
