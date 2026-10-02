package hexa.erp.sale.mapper;

import java.util.List;

import hexa.erp.sale.domain.SaleStatusCriteria;
import hexa.erp.sale.domain.SaleStatusRowVO;

public interface SaleStatusMapper {
	List<SaleStatusRowVO> getStatusRows(SaleStatusCriteria criteria);

}
