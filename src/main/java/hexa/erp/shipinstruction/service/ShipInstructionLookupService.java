package hexa.erp.shipinstruction.service;

import java.util.List;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.shipinstruction.domain.ShipInstructionLookupVO;

public interface ShipInstructionLookupService {
	List<ShipInstructionLookupVO> getList(LookupCriteria criteria, String progressStatus);

	int getTotal(LookupCriteria criteria, String progressStatus);
}
