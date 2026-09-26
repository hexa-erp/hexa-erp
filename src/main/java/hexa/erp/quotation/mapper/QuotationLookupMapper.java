package hexa.erp.quotation.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.quotation.domain.QuotationLookupLineVO;
import hexa.erp.quotation.domain.QuotationLookupVO;

public interface QuotationLookupMapper {
	List<QuotationLookupVO> getListWithPaging(@Param("criteria") LookupCriteria criteria,
			@Param("progressStatus") String progressStatus);

	int getTotalCount(@Param("criteria") LookupCriteria criteria, @Param("progressStatus") String progressStatus);

	List<QuotationLookupLineVO> getLines(@Param("documentId") Long documentId);
}
