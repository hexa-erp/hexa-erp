package hexa.erp.sale.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/** 단가 일괄변경 저장 요청. changes[0].unitPrice 형태의 이름으로 바인딩된다. */
@Data
public class SalePriceFormVO {
	private List<SalePriceChangeVO> changes = new ArrayList<>();

}
