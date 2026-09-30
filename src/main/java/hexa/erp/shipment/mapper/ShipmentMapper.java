package hexa.erp.shipment.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hexa.erp.shipment.domain.ShipmentCriteria;
import hexa.erp.shipment.domain.ShipmentLineVO;
import hexa.erp.shipment.domain.ShipmentVO;

public interface ShipmentMapper {
	List<ShipmentVO> getListWithPaging(ShipmentCriteria criteria);

	int getTotalCount(ShipmentCriteria criteria);

	ShipmentVO read(Long shipmentId);

	List<ShipmentLineVO> getLines(Long shipmentId);

	void insertSelectKey(ShipmentVO shipment);

	int update(ShipmentVO shipment);

	void insertLineSelectKey(ShipmentLineVO line);

	int updateLine(ShipmentLineVO line);

	int removeSelectedLines(@Param("shipmentId") Long shipmentId, @Param("lineIds") List<Long> lineIds);

	int remove(@Param("ids") List<Long> ids);

	int removeLines(@Param("ids") List<Long> ids);

	int changeStatus(@Param("ids") List<Long> ids, @Param("progressStatus") String progressStatus);

}
