package hexa.erp.shipinstruction.service;

import hexa.erp.shipinstruction.domain.ShipInstructionVO;

public interface ShipInstructionService {

	ShipInstructionVO get(Long shipInstructionId);
	
	Long save(ShipInstructionVO shipInstruction);
	
	
}
