package hexa.erp.testinfra;

import java.util.Collections;
import java.util.List;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.shipinstruction.domain.ShipInstructionLookupVO;
import hexa.erp.shipinstruction.service.ShipInstructionLookupService;

public class StubShipInstructionLookupService implements ShipInstructionLookupService {

	@Override
	public List<ShipInstructionLookupVO> getList(LookupCriteria criteria, String progressStatus) {
		return Collections.emptyList();
	}

	@Override
	public int getTotal(LookupCriteria criteria, String progressStatus) {
		return 0;
	}
}
