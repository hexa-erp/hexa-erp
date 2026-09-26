package hexa.erp.testinfra;

import java.util.Collections;
import java.util.List;

import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.partner.service.PartnerLookupService;
import hexa.erp.common.domain.LookupCriteria;

/** 웹 컨텍스트 테스트 전용 응답. 실제 DB 데이터나 검색 구현이 아니며 main에서는 사용하지 않는다. */
public class StubPartnerLookupService implements PartnerLookupService {
	@Override
	public PartnerLookupVO get(Long id) {
		PartnerLookupVO row = getList(new LookupCriteria()).get(0);
		row.setPartnerId(id);
		row.setPartnerName("웹 테스트 거래처 " + id);
		return row;
	}

	@Override
	public int getTotal(LookupCriteria criteria) {
		return 1;
	}

	@Override
	public List<PartnerLookupVO> getList(LookupCriteria criteria) {
		PartnerLookupVO row = new PartnerLookupVO();
		row.setPartnerId(42L);
		row.setPartnerCode("00001");
		row.setPartnerName("웹 테스트 거래처");
		row.setActiveFlag("Y");
		return Collections.singletonList(row);
	}
}
