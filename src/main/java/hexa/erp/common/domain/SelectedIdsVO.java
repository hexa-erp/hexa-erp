package hexa.erp.common.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/** 複数行を選択するリクエストで使用するIDの一覧。 */
@Data
public class SelectedIdsVO {
	private List<Long> ids = new ArrayList<>();
}
