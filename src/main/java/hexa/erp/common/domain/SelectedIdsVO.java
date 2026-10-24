package hexa.erp.common.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/** 여러 행을 선택한 요청에서 사용하는 식별자 목록. */
@Data
public class SelectedIdsVO {
	private List<Long> ids = new ArrayList<>();
}
