package hexa.erp.salesorder.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.salesorder.domain.SalesOrderLookupLineVO;
import hexa.erp.salesorder.domain.SalesOrderLookupVO;

public interface SalesOrderLookupMapper {
	List<SalesOrderLookupVO> getListWithPaging(@Param("criteria") LookupCriteria criteria,
			@Param("progressStatus") String progressStatus);

	int getTotalCount(@Param("criteria") LookupCriteria criteria, @Param("progressStatus") String progressStatus);

	List<SalesOrderLookupLineVO> getLines(@Param("documentId") Long documentId);
}
