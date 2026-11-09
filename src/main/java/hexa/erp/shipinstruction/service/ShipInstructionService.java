package hexa.erp.shipinstruction.service;

import java.util.List;

import hexa.erp.shipinstruction.domain.ShipInstructionCriteria;
import hexa.erp.shipinstruction.domain.ShipInstructionVO;

public interface ShipInstructionService {

	List<ShipInstructionVO> getList(ShipInstructionCriteria criteria);

	int getTotal(ShipInstructionCriteria criteria);

	ShipInstructionVO get(Long shipInstructionId);

	Long save(ShipInstructionVO shipInstruction);

	int remove(List<Long> seletedIds);

	int changeStatus(List<Long> seletedIds, String progressStatus);

}
