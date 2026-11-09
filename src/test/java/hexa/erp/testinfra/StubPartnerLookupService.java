package hexa.erp.testinfra;

import java.util.Collections;
import java.util.List;

import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.partner.service.PartnerLookupService;
import hexa.erp.common.domain.LookupCriteria;

/** Webコンテキストテスト専用レスポンス。実際のDBデータや検索処理ではなく、mainでは使用しない。 */
public class StubPartnerLookupService implements PartnerLookupService {
	@Override
	public PartnerLookupVO get(Long id) {
		PartnerLookupVO row = getList(new LookupCriteria()).get(0);
		row.setPartnerId(id);
		row.setPartnerName("Webテスト取引先 " + id);
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
		row.setPartnerName("Webテスト取引先");
		row.setActiveFlag("Y");
		return Collections.singletonList(row);
	}
}
