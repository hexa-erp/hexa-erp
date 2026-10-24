package hexa.erp.sale.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/** 단가 일괄변경 JSON 요청. changes 배열의 선택한 행을 저장한다. */
@Data
public class SalePriceFormVO {
	private List<SalePriceChangeVO> changes = new ArrayList<>();

}
