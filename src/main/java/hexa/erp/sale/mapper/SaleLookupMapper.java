package hexa.erp.sale.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.sale.domain.SaleLookupLineVO;
import hexa.erp.sale.domain.SaleLookupVO;

public interface SaleLookupMapper {
	List<SaleLookupVO> getListWithPaging(@Param("criteria") LookupCriteria criteria,
			@Param("progressStatus") String progressStatus);

	int getTotalCount(@Param("criteria") LookupCriteria criteria, @Param("progressStatus") String progressStatus);

	List<SaleLookupLineVO> getLines(@Param("documentId") Long documentId);
}
