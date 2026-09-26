package hexa.erp.shipinstruction.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.shipinstruction.domain.ShipInstructionLookupLineVO;
import hexa.erp.shipinstruction.domain.ShipInstructionLookupVO;

public interface ShipInstructionLookupMapper {
	List<ShipInstructionLookupVO> getListWithPaging(@Param("criteria") LookupCriteria criteria,
			@Param("progressStatus") String progressStatus);

	int getTotalCount(@Param("criteria") LookupCriteria criteria, @Param("progressStatus") String progressStatus);

	List<ShipInstructionLookupLineVO> getLines(@Param("documentId") Long documentId);
}
