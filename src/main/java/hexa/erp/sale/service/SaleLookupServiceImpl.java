package hexa.erp.sale.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.sale.domain.SaleLookupVO;
import hexa.erp.sale.mapper.SaleLookupMapper;
import lombok.Setter;

@Service
public class SaleLookupServiceImpl implements SaleLookupService {
	@Setter(onMethod_ = @Autowired)
	private SaleLookupMapper mapper;

	@Override
	public List<SaleLookupVO> getList(LookupCriteria criteria, String progressStatus) {
		List<SaleLookupVO> documents = mapper.getListWithPaging(criteria, progressStatus);
		for (SaleLookupVO document : documents) {
			document.setLines(mapper.getLines(document.getDocumentId()));
		}
		return documents;
	}

	@Override
	public int getTotal(LookupCriteria criteria, String progressStatus) {
		return mapper.getTotalCount(criteria, progressStatus);
	}
}
