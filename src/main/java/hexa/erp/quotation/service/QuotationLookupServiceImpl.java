package hexa.erp.quotation.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.quotation.domain.QuotationLookupVO;
import hexa.erp.quotation.mapper.QuotationLookupMapper;
import lombok.Setter;

@Service
public class QuotationLookupServiceImpl implements QuotationLookupService {
	@Setter(onMethod_ = @Autowired)
	private QuotationLookupMapper mapper;

	@Override
	public List<QuotationLookupVO> getList(LookupCriteria criteria, String progressStatus) {
		List<QuotationLookupVO> documents = mapper.getListWithPaging(criteria, progressStatus);
		for (QuotationLookupVO document : documents) {
			document.setLines(mapper.getLines(document.getDocumentId()));
		}
		return documents;
	}

	@Override
	public int getTotal(LookupCriteria criteria, String progressStatus) {
		return mapper.getTotalCount(criteria, progressStatus);
	}
}
