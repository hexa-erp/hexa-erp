package hexa.erp.shipinstruction.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.shipinstruction.domain.ShipInstructionLineVO;
import hexa.erp.shipinstruction.domain.ShipInstructionVO;

public interface ShipInstructionMapper {
	ShipInstructionVO read(Long shipInstructionId);
	
	List<ShipInstructionLineVO> getLines(Long shipInstructionId);
	
	void insertSelectKey(ShipInstructionVO shipInstruction);
	
	int update(ShipInstructionVO shipInstruction);
	
	void insertLineSelectKey(ShipInstructionLineVO line);
	
	int updateLine(ShipInstructionLineVO line);
	
	int removeSelectedLines(@Param("shipInstructionId") Long shipInstructionId, @Param("lineIds")List<Long> lineIds);
}
