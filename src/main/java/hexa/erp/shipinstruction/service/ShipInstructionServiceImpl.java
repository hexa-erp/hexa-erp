package hexa.erp.shipinstruction.service;

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
import hexa.erp.shipinstruction.domain.ShipInstructionLineVO;
import hexa.erp.shipinstruction.domain.ShipInstructionVO;
import hexa.erp.shipinstruction.mapper.ShipInstructionMapper;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.warehouse.service.WarehouseLookupService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
public class ShipInstructionServiceImpl implements ShipInstructionService {

	@Setter(onMethod_ = @Autowired)
	private ShipInstructionMapper mapper;
	@Setter(onMethod_ = @Autowired)
	private PartnerLookupService partnerLookupService;
	@Setter(onMethod_ = @Autowired)
	private WarehouseLookupService warehouseLookupService;
	@Setter(onMethod_ = @Autowired)
	private AssigneeLookupService assigneeLookupService;
	@Setter(onMethod_ = @Autowired)
	private ItemLookupService itemLookupService;

	@Override
	public ShipInstructionVO get(Long shipInstructionId) {
		// TODO Auto-generated method stub
		if (shipInstructionId == null) {
			return null;
		}
		ShipInstructionVO shipInstruction = mapper.read(shipInstructionId);
		if (shipInstruction != null) {
			shipInstruction.setLines(mapper.getLines(shipInstructionId));
		}
		return shipInstruction;

	}

	@Override
	@Transactional
	public Long save(ShipInstructionVO shipInstruction) {
		// TODO Auto-generated method stub
		if (shipInstruction == null || isBlank(shipInstruction.getBusinessDate())) {
			throw new IllegalArgumentException("출하지시 일자를 입력해 주세요.");
		}
		try {

			shipInstruction.setBusinessDate(LocalDate.parse(shipInstruction.getBusinessDate().trim()).toString());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("출하지시 일자를 올바르게 입력해 주세요.");
		}
		if (isBlank(shipInstruction.getPlannedDate())) {
			shipInstruction.setPlannedDate(null);
		} else {
			try {
				shipInstruction.setPlannedDate(LocalDate.parse(shipInstruction.getPlannedDate().trim()).toString());
			} catch (DateTimeParseException e) {
				throw new IllegalArgumentException("출하 예정일을 올바르게 입력해 주세요.");
			}
		}
		if (shipInstruction.getWarehouseId() == null) {
			throw new IllegalArgumentException("출하창고를 선택해 주세요.");
		}

		ShipInstructionVO existing = null;
		if (shipInstruction.getShipInstructionId() != null) {
			existing = mapper.read(shipInstruction.getShipInstructionId());
			if (existing == null) {
				throw new IllegalArgumentException("수정할 출하지시서를 찾을 수 없습니다.");
			}
			shipInstruction.setShipInstructionNo(existing.getShipInstructionNo());
			shipInstruction.setProgressStatus(existing.getProgressStatus());
		} else {
			shipInstruction.setProgressStatus("IN_PROGRESS");
		}
		fillHeaderNames(shipInstruction, existing);
		if (isBlank(shipInstruction.getWarehouseName())) {
			throw new IllegalArgumentException("선택한 출하창고를 찾을 수 없습니다.");
		}

		List<Long> removedLineIds = cleanIds(shipInstruction.getRemovedLineIds());
		List<ShipInstructionLineVO> lines = new ArrayList<>();
		Set<Long> lineIds = new HashSet<>();
		if (shipInstruction.getLines() != null) {
			for (ShipInstructionLineVO line : shipInstruction.getLines()) {
				if (line == null || line.getItemId() == null) {
					continue;
				}
				if (line.getShipInstructionLineId() != null) {
					if (!lineIds.add(line.getShipInstructionLineId())
							|| removedLineIds.contains(line.getShipInstructionLineId())) {
						throw new IllegalArgumentException("중복되거나 삭제 표시된 품목 행을 확인해주세요.");
					}
				}
				prepareLine(line);
				lines.add(line);
			}
		}

		if (lines.isEmpty()) {
			throw new IllegalArgumentException("출하지시 품목을 한 개 이상 선택해 주세요.");
		}
		shipInstruction.setLines(lines);

		if (existing == null) {
			mapper.insertSelectKey(shipInstruction);
		} else if (mapper.update(shipInstruction) != 1) {
			throw new IllegalArgumentException("수정할 출하지시서를 찾을 수 없습니다.");
		}
		if (!removedLineIds.isEmpty()) {
			mapper.removeSelectedLines(shipInstruction.getShipInstructionId(), removedLineIds);
		}
		for (ShipInstructionLineVO line : lines) {
			line.setShipInstructionId(shipInstruction.getShipInstructionId());
			if (line.getShipInstructionLineId() == null) {
				mapper.insertLineSelectKey(line);
			} else if (mapper.updateLine(line) != 1) {
				throw new IllegalArgumentException("출하지시서에 속하지 않거나 삭제된 품목 행입니다.");
			}
		}
		log.info("출하지시서 저장: " + shipInstruction.getShipInstructionId());
		return shipInstruction.getShipInstructionId();
	}

	private void prepareLine(ShipInstructionLineVO line) {
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

	private void fillHeaderNames(ShipInstructionVO shipInstruction, ShipInstructionVO existing) {
		if (shipInstruction.getPartnerId() == null) {
			shipInstruction.setPartnerName(null);
		} else if (isBlank(shipInstruction.getPartnerName())) {
			if (existing != null && shipInstruction.getPartnerId().equals(existing.getPartnerId())
					&& !isBlank(existing.getPartnerName())) {
				shipInstruction.setPartnerName(existing.getPartnerName());
			} else {
				PartnerLookupVO partner = partnerLookupService.get(shipInstruction.getPartnerId());
				shipInstruction.setPartnerName(partner == null ? null : partner.getPartnerName());
			}
		}
		if (shipInstruction.getWarehouseId() == null) {
			shipInstruction.setWarehouseName(null);
		} else if (isBlank(shipInstruction.getWarehouseName())) {
			if (existing != null && shipInstruction.getWarehouseId().equals(existing.getWarehouseId())
					&& !isBlank(existing.getWarehouseName())) {
				shipInstruction.setWarehouseName(existing.getWarehouseName());
			} else {
				WarehouseLookupVO warehouse = warehouseLookupService.get(shipInstruction.getWarehouseId());
				shipInstruction.setWarehouseName(warehouse == null ? null : warehouse.getWarehouseName());
			}
		}
		if (shipInstruction.getAssigneeId() == null) {
			shipInstruction.setAssigneeName(null);
		} else if (isBlank(shipInstruction.getAssigneeName())) {
			if (existing != null && shipInstruction.getAssigneeId().equals(existing.getAssigneeId())
					&& !isBlank(existing.getAssigneeName())) {
				shipInstruction.setAssigneeName(existing.getAssigneeName());
			} else {
				AssigneeLookupVO assignee = assigneeLookupService.get(shipInstruction.getAssigneeId());
				shipInstruction.setAssigneeName(assignee == null ? null : assignee.getAssigneeName());
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
