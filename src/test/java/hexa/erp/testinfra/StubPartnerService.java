package hexa.erp.testinfra;

import java.util.Collections;
import java.util.List;

import hexa.erp.partner.domain.PartnerVO;
import hexa.erp.partner.service.PartnerService;
import hexa.erp.common.domain.LookupCriteria;

/** 웹 컨텍스트 테스트 전용 응답. 실제 DB 데이터나 검색 구현이 아니며 main에서는 사용하지 않는다. */
public class StubPartnerService implements PartnerService {
	@Override
	public int getTotal(LookupCriteria criteria) {
		return 1;
	}

	@Override
	public List<PartnerVO> getList(LookupCriteria criteria) {
		PartnerVO row = new PartnerVO();
		row.setPartnerId(42L);
		row.setPartnerCode("00001");
		row.setPartnerName("웹 테스트 거래처");
		row.setActiveFlag("Y");
		return Collections.singletonList(row);
	}
}
