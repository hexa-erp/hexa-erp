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
			throw new IllegalArgumentException("出荷日を入力してください。");
		}
		try {
			shipment.setBusinessDate(LocalDate.parse(shipment.getBusinessDate().trim()).toString());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("正しい出荷日を入力してください。");
		}
		if (shipment.getWarehouseId() == null) {
			throw new IllegalArgumentException("出荷倉庫を選択してください。");
		}

		ShipmentVO existing = null;
		if (shipment.getShipmentId() != null) {
			existing = mapper.read(shipment.getShipmentId());
			if (existing == null) {
				throw new IllegalArgumentException("更新対象の出荷伝票が見つかりません。");
			}
			// 更新画面では伝票番号と進行状態を変更しないため、既存の値を維持する。
			shipment.setShipmentNo(existing.getShipmentNo());
			shipment.setProgressStatus(existing.getProgressStatus());
		} else {
			shipment.setProgressStatus("CONFIRMED");
		}
		fillHeaderNames(shipment, existing);
		if (isBlank(shipment.getWarehouseName())) {
			throw new IllegalArgumentException("選択した出荷倉庫が見つかりません。");
		}

		List<Long> removedLineIds = cleanIds(shipment.getRemovedLineIds());
		List<ShipmentLineVO> lines = new ArrayList<>();
		Set<Long> lineIds = new HashSet<>();
		if (shipment.getLines() != null) {
			for (ShipmentLineVO line : shipment.getLines()) {
				// 品目が選択されていない空行は保存しない。
				if (line == null || line.getItemId() == null) {
					continue;
				}
				if (line.getShipmentLineId() != null) {
					if (!lineIds.add(line.getShipmentLineId()) || removedLineIds.contains(line.getShipmentLineId())) {
						throw new IllegalArgumentException("重複または削除指定された品目行を確認してください。");
					}
				}
				prepareLine(line);
				lines.add(line);
			}
		}
		if (lines.isEmpty()) {
			throw new IllegalArgumentException("出荷品目を1件以上選択してください。");
		}
		shipment.setLines(lines);

		if (existing == null) {
			mapper.insertSelectKey(shipment);
		} else if (mapper.update(shipment) != 1) {
			throw new IllegalArgumentException("更新対象の出荷伝票が見つかりません。");
		}
		if (!removedLineIds.isEmpty()) {
			mapper.removeSelectedLines(shipment.getShipmentId(), removedLineIds);
		}
		// 行IDがなければ新規行を登録し、あればこの伝票に属する既存行のみ更新する。
		for (ShipmentLineVO line : lines) {
			line.setShipmentId(shipment.getShipmentId());
			if (line.getShipmentLineId() == null) {
				mapper.insertLineSelectKey(line);
			} else if (mapper.updateLine(line) != 1) {
				throw new IllegalArgumentException("この出荷伝票に属していないか、削除済みの品目行です。");
			}
		}
		log.info("出荷伝票の保存: " + shipment.getShipmentId());
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
		log.info("出荷伝票の削除: " + count + "件");
		return count;
	}

	@Override
	public int changeStatus(List<Long> selectedIds, String progressStatus) {
		List<Long> ids = cleanIds(selectedIds);
		if (ids.isEmpty()) {
			return 0;
		}
		if (!"CONFIRMED".equals(progressStatus) && !"UNCONFIRMED".equals(progressStatus)) {
			throw new IllegalArgumentException("進行状態は未確認、確認のいずれかを選択してください。");
		}
		log.info("出荷伝票の進行状態変更: " + progressStatus);
		return mapper.changeStatus(ids, progressStatus);
	}

	// 品目名が空の場合は品目マスターから補完し、数量は小数第3位にそろえる。
	private void prepareLine(ShipmentLineVO line) {
		if (isBlank(line.getItemName())) {
			ItemLookupVO item = itemLookupService.get(line.getItemId());
			if (item == null) {
				throw new IllegalArgumentException("選択した品目が見つかりません。");
			}
			line.setItemName(item.getItemName());
		}
		BigDecimal quantity = line.getQuantity() == null ? BigDecimal.ZERO : line.getQuantity();
		line.setQuantity(quantity.setScale(3, RoundingMode.HALF_UP));
	}

	// 名前が空の場合は同じIDの既存伝票の名前を優先し、なければマスターから取得する。
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

	// nullと重複IDを除去し、順序を維持する。
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
