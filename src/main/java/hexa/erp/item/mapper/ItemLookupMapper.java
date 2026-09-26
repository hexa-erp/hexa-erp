package hexa.erp.item.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.item.domain.ItemLookupVO;
import hexa.erp.common.domain.LookupCriteria;

public interface ItemLookupMapper {
	ItemLookupVO read(Long itemId);

	List<ItemLookupVO> getListWithPaging(@Param("criteria") LookupCriteria criteria,
			@Param("warehouseId") Long warehouseId);

	int getTotalCount(@Param("criteria") LookupCriteria criteria);
}
