package hexa.erp.salesorder.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.salesorder.domain.SalesOrderLookupVO;
import hexa.erp.salesorder.mapper.SalesOrderLookupMapper;
import lombok.Setter;

@Service
public class SalesOrderLookupServiceImpl implements SalesOrderLookupService {
	@Setter(onMethod_ = @Autowired)
	private SalesOrderLookupMapper mapper;

	@Override
	public List<SalesOrderLookupVO> getList(LookupCriteria criteria, String progressStatus) {
		List<SalesOrderLookupVO> documents = mapper.getListWithPaging(criteria, progressStatus);
		for (SalesOrderLookupVO document : documents) {
			document.setLines(mapper.getLines(document.getDocumentId()));
		}
		return documents;
	}

	@Override
	public int getTotal(LookupCriteria criteria, String progressStatus) {
		return mapper.getTotalCount(criteria, progressStatus);
	}
}
