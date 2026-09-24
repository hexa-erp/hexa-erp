package hexa.erp.item.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.item.domain.ItemVO;
import hexa.erp.common.domain.LookupCriteria;

public interface ItemMapper {
	List<ItemVO> getList(@Param("criteria") LookupCriteria criteria, @Param("warehouseId") Long warehouseId);

	int getTotal(@Param("criteria") LookupCriteria criteria);
}
