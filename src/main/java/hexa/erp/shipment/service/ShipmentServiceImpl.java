package hexa.erp.shipment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hexa.erp.assignee.domain.AssigneeLookupVO;
import hexa.erp.assignee.service.AssigneeLookupService;
import hexa.erp.item.domain.ItemLookupVO;
import hexa.erp.item.service.ItemLookupService;
import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.partner.service.PartnerLookupService;
import hexa.erp.shipment.domain.ShipmentCriteria;
import hexa.erp.shipment.domain.ShipmentLineVO;
import hexa.erp.shipment.domain.ShipmentVO;
import hexa.erp.shipment.mapper.ShipmentMapper;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.warehouse.service.WarehouseLookupService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
public class ShipmentServiceImpl implements ShipmentService {
	@Setter(onMethod_ = @Autowired)
	private ShipmentMapper mapper;
	@Setter(onMethod_ = @Autowired)
	private PartnerLookupService partnerLookupService;
	@Setter(onMethod_ = @Autowired)
	private WarehouseLookupService warehouseLookupService;
	@Setter(onMethod_ = @Autowired)
	private AssigneeLookupService assigneeLookupService;
	@Setter(onMethod_ = @Autowired)
	private ItemLookupService itemLookupService;

	@Override
	public List<ShipmentVO> getList(ShipmentCriteria criteria) {
		return mapper.getListWithPaging(criteria);
	}

	@Override
	public int getTotal(ShipmentCriteria criteria) {
		return mapper.getTotalCount(criteria);
	}

	@Override
	public ShipmentVO get(Long shipmentId) {
		if (shipmentId == null) {
			return null;
		}
		ShipmentVO shipment = mapper.read(shipmentId);
		if (shipment != null) {
			shipment.setLines(mapper.getLines(shipmentId));
		}
		return shipment;
	}

	@Override
	@Transactional
	public Long save(ShipmentVO shipment) {
		if (shipment == null || isBlank(shipment.getBusinessDate())) {
			throw new IllegalArgumentException("출하 일자를 입력해 주세요.");
		}
		try {
			shipment.setBusinessDate(LocalDate.parse(shipment.getBusinessDate().trim()).toString());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("출하 일자를 올바르게 입력해 주세요.");
		}
		if (shipment.getWarehouseId() == null) {
			throw new IllegalArgumentException("출하창고를 선택해 주세요.");
		}

		ShipmentVO existing = null;
		if (shipment.getShipmentId() != null) {
			existing = mapper.read(shipment.getShipmentId());
			if (existing == null) {
				throw new IllegalArgumentException("수정할 출하전표를 찾을 수 없습니다.");
			}
			shipment.setShipmentNo(existing.getShipmentNo());
			shipment.setProgressStatus(existing.getProgressStatus());
		} else {
			shipment.setProgressStatus("CONFIRMED");
		}
		fillHeaderNames(shipment, existing);
		if (isBlank(shipment.getWarehouseName())) {
			throw new IllegalArgumentException("선택한 출하창고를 찾을 수 없습니다.");
		}

		List<Long> removedLineIds = cleanIds(shipment.getRemovedLineIds());
		List<ShipmentLineVO> lines = new ArrayList<>();
		Set<Long> lineIds = new HashSet<>();
		if (shipment.getLines() != null) {
			for (ShipmentLineVO line : shipment.getLines()) {
				if (line == null || line.getItemId() == null) {
					continue;
				}
				if (line.getShipmentLineId() != null) {
					if (!lineIds.add(line.getShipmentLineId()) || removedLineIds.contains(line.getShipmentLineId())) {
						throw new IllegalArgumentException("중복되거나 삭제 표시된 품목 행을 확인해 주세요.");
					}
				}
				prepareLine(line);
				lines.add(line);
			}
		}
		if (lines.isEmpty()) {
			throw new IllegalArgumentException("출하 품목을 한 개 이상 선택해 주세요.");
		}
		shipment.setLines(lines);

		if (existing == null) {
			mapper.insertSelectKey(shipment);
		} else if (mapper.update(shipment) != 1) {
			throw new IllegalArgumentException("수정할 출하전표를 찾을 수 없습니다.");
		}
		if (!removedLineIds.isEmpty()) {
			mapper.removeSelectedLines(shipment.getShipmentId(), removedLineIds);
		}
		for (ShipmentLineVO line : lines) {
			line.setShipmentId(shipment.getShipmentId());
			if (line.getShipmentLineId() == null) {
				mapper.insertLineSelectKey(line);
			} else if (mapper.updateLine(line) != 1) {
				throw new IllegalArgumentException("출하전표에 속하지 않거나 삭제된 품목 행입니다.");
			}
		}
		log.info("출하전표 저장: " + shipment.getShipmentId());
		return shipment.getShipmentId();
	}

	@Override
	@Transactional
	public int remove(List<Long> selectedIds) {
		List<Long> ids = cleanIds(selectedIds);
		if (ids.isEmpty()) {
			return 0;
		}
		int count = mapper.remove(ids);
		mapper.removeLines(ids);
		log.info("출하전표 삭제: " + count + "건");
		return count;
	}

	@Override
	public int changeStatus(List<Long> selectedIds, String progressStatus) {
		List<Long> ids = cleanIds(selectedIds);
		if (ids.isEmpty()) {
			return 0;
		}
		if (!"CONFIRMED".equals(progressStatus) && !"UNCONFIRMED".equals(progressStatus)) {
			throw new IllegalArgumentException("진행상태는 미확인, 확인 중에서 선택해 주세요.");
		}
		log.info("출하전표 진행상태 변경: " + progressStatus);
		return mapper.changeStatus(ids, progressStatus);
	}

	private void prepareLine(ShipmentLineVO line) {
		if (isBlank(line.getItemName())) {
			ItemLookupVO item = itemLookupService.get(line.getItemId());
			if (item == null) {
				throw new IllegalArgumentException("선택한 품목을 찾을 수 없습니다.");
			}
			line.setItemName(item.getItemName());
		}
		BigDecimal quantity = line.getQuantity() == null ? BigDecimal.ZERO : line.getQuantity();
		line.setQuantity(quantity.setScale(3, RoundingMode.HALF_UP));
	}

	private void fillHeaderNames(ShipmentVO shipment, ShipmentVO existing) {
		if (shipment.getPartnerId() == null) {
			shipment.setPartnerName(null);
		} else if (isBlank(shipment.getPartnerName())) {
			if (existing != null && shipment.getPartnerId().equals(existing.getPartnerId())
					&& !isBlank(existing.getPartnerName())) {
				shipment.setPartnerName(existing.getPartnerName());
			} else {
				PartnerLookupVO partner = partnerLookupService.get(shipment.getPartnerId());
				shipment.setPartnerName(partner == null ? null : partner.getPartnerName());
			}
		}
		if (shipment.getWarehouseId() == null) {
			shipment.setWarehouseName(null);
		} else if (isBlank(shipment.getWarehouseName())) {
			if (existing != null && shipment.getWarehouseId().equals(existing.getWarehouseId())
					&& !isBlank(existing.getWarehouseName())) {
				shipment.setWarehouseName(existing.getWarehouseName());
			} else {
				WarehouseLookupVO warehouse = warehouseLookupService.get(shipment.getWarehouseId());
				shipment.setWarehouseName(warehouse == null ? null : warehouse.getWarehouseName());
			}
		}
		if (shipment.getAssigneeId() == null) {
			shipment.setAssigneeName(null);
		} else if (isBlank(shipment.getAssigneeName())) {
			if (existing != null && shipment.getAssigneeId().equals(existing.getAssigneeId())
					&& !isBlank(existing.getAssigneeName())) {
				shipment.setAssigneeName(existing.getAssigneeName());
			} else {
				AssigneeLookupVO assignee = assigneeLookupService.get(shipment.getAssigneeId());
				shipment.setAssigneeName(assignee == null ? null : assignee.getAssigneeName());
			}
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private List<Long> cleanIds(List<Long> values) {
		Set<Long> ids = new LinkedHashSet<>();
		if (values != null) {
			for (Long value : values) {
				if (value != null) {
					ids.add(value);
				}
			}
		}
		return new ArrayList<>(ids);
	}
}
