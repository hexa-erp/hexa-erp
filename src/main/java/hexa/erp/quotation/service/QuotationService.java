package hexa.erp.quotation.service;

import java.util.List;

import hexa.erp.quotation.domain.QuotationCriteria;
import hexa.erp.quotation.domain.QuotationVO;

public interface QuotationService {
	
	public QuotationVO get(Long quotationId);
	
	public List<QuotationVO> getList(QuotationCriteria criteria);
	
	public int getTotal(QuotationCriteria criteria);
	
	public Long save(QuotationVO quotation);
	
	public int remove(List<Long> selectedIds);
	
	public int changeStatus(List<Long> quoatationId, String progressStatus);
}
