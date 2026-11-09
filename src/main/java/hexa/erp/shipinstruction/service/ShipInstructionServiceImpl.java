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
import hexa.erp.shipinstruction.domain.ShipInstructionCriteria;
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
	public List<ShipInstructionVO> getList(ShipInstructionCriteria criteria) {
		return mapper.getListWithPaging(criteria);
	}

	@Override
	public int getTotal(ShipInstructionCriteria criteria) {
		return mapper.getTotalCount(criteria);
	}

	@Override
	public ShipInstructionVO get(Long shipInstructionId) {
		if (shipInstructionId == null) {
			return null;
		}
		// 保存済みの基本情報と、削除されていない品目行をまとめて取得する。
		ShipInstructionVO shipInstruction = mapper.read(shipInstructionId);
		if (shipInstruction != null) {
			shipInstruction.setLines(mapper.getLines(shipInstructionId));
		}
		return shipInstruction;

	}

	// 基本情報と品目行の保存は1つのトランザクションで処理し、途中で失敗した場合はすべてロールバックする。
	@Override
	@Transactional
	public Long save(ShipInstructionVO shipInstruction) {
		// 日付の空欄と形式を確認し、不正な場合は案内を表示して保存しない。
		if (shipInstruction == null || isBlank(shipInstruction.getBusinessDate())) {
			throw new IllegalArgumentException("出荷指示日を入力してください。");
		}
		try {

			shipInstruction.setBusinessDate(LocalDate.parse(shipInstruction.getBusinessDate().trim()).toString());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("正しい出荷指示日を入力してください。");
		}
		// 出荷予定日は空欄を許可し、入力がある場合は日付形式を確認する。
		if (isBlank(shipInstruction.getPlannedDate())) {
			shipInstruction.setPlannedDate(null);
		} else {
			try {
				shipInstruction.setPlannedDate(LocalDate.parse(shipInstruction.getPlannedDate().trim()).toString());
			} catch (DateTimeParseException e) {
				throw new IllegalArgumentException("正しい出荷予定日を入力してください。");
			}
		}
		// 出荷倉庫が空欄の場合は、出荷倉庫を選択するよう案内する。
		if (shipInstruction.getWarehouseId() == null) {
			throw new IllegalArgumentException("出荷倉庫を選択してください。");
		}

		ShipInstructionVO existing = null;
		if (shipInstruction.getShipInstructionId() != null) {
			existing = mapper.read(shipInstruction.getShipInstructionId());
			if (existing == null) {
				throw new IllegalArgumentException("更新対象の出荷指示書が見つかりません。");
			}
			// 編集時は保存済みの番号・進行状態を保持して基本情報を更新する。
			shipInstruction.setShipInstructionNo(existing.getShipInstructionNo());
			shipInstruction.setProgressStatus(existing.getProgressStatus());
		} else {
			// 新規伝票の進行状態は進行中（IN_PROGRESS）とする。
			shipInstruction.setProgressStatus("IN_PROGRESS");
		}
		fillHeaderNames(shipInstruction, existing);
		if (isBlank(shipInstruction.getWarehouseName())) {
			throw new IllegalArgumentException("選択した出荷倉庫が見つかりません。");
		}

		List<Long> removedLineIds = cleanIds(shipInstruction.getRemovedLineIds());
		List<ShipInstructionLineVO> lines = new ArrayList<>();
		Set<Long> lineIds = new HashSet<>();
		if (shipInstruction.getLines() != null) {
			for (ShipInstructionLineVO line : shipInstruction.getLines()) {
				// 品目が未選択の空行は保存しない。
				if (line == null || line.getItemId() == null) {
					continue;
				}
				// 同じ明細IDの重複、または削除リストにも含まれる行はエラーとする。
				if (line.getShipInstructionLineId() != null) {
					if (!lineIds.add(line.getShipInstructionLineId())
							|| removedLineIds.contains(line.getShipInstructionLineId())) {
						throw new IllegalArgumentException("重複または削除指定された品目行を確認してください。");
					}
				}
				prepareLine(line);
				lines.add(line);
			}
		}

		// 空行を除いた後に品目行が1件も残らない場合は、保存を中止する。
		if (lines.isEmpty()) {
			throw new IllegalArgumentException("出荷指示の品目を1件以上選択してください。");
		}
		shipInstruction.setLines(lines);

		if (existing == null) {
			mapper.insertSelectKey(shipInstruction);
		} else if (mapper.update(shipInstruction) != 1) {
			throw new IllegalArgumentException("更新対象の出荷指示書が見つかりません。");
		}
		// 削除リストにある既存の品目行は、明細を消去せず削除フラグを設定する。
		if (!removedLineIds.isEmpty()) {
			mapper.removeSelectedLines(shipInstruction.getShipInstructionId(), removedLineIds);
		}
		// 残る行は、明細IDがない場合は追加し、ある場合はこの伝票に属する既存行のみ更新する。
		for (ShipInstructionLineVO line : lines) {
			line.setShipInstructionId(shipInstruction.getShipInstructionId());
			if (line.getShipInstructionLineId() == null) {
				mapper.insertLineSelectKey(line);
			} else if (mapper.updateLine(line) != 1) {
				throw new IllegalArgumentException("この出荷指示書に属していないか、削除済みの品目行です。");
			}
		}
		log.info("出荷指示書の保存: " + shipInstruction.getShipInstructionId());
		return shipInstruction.getShipInstructionId();
	}

	// チェックした出荷指示書の基本情報と品目行に削除フラグを設定する。行自体は削除しない。
	@Override
	@Transactional
	public int remove(List<Long> seletedIds) {
		List<Long> ids = cleanIds(seletedIds);
		if (ids.isEmpty()) {
			return 0;
		}
		int count = mapper.remove(ids);
		mapper.removeLines(ids);
		log.info("出荷指示書の削除: " + count + "件");
		return count;
	}

	// 選択した出荷指示書の進行状態と最終更新日時を変更する。選択可能な状態以外は受け付けない。
	@Override
	public int changeStatus(List<Long> seletedIds, String progressStatus) {
		List<Long> ids = cleanIds(seletedIds);
		if (ids.isEmpty()) {
			return 0;
		}
		if (!"IN_PROGRESS".equals(progressStatus) && !"COMPLETED".equals(progressStatus)) {
			throw new IllegalArgumentException("進行状態は進行中、完了のいずれかを選択してください。");
		}
		log.info("出荷指示書の進行状態変更: " + progressStatus);
		return mapper.changeStatus(ids, progressStatus);
	}

	// 品目の名称が空の場合は、選択画面のデータから補完する。
	private void prepareLine(ShipInstructionLineVO line) {
		if (isBlank(line.getItemName())) {
			ItemLookupVO item = itemLookupService.get(line.getItemId());
			if (item == null) {
				throw new IllegalArgumentException("選択した品目が見つかりません。");
			}
			line.setItemName(item.getItemName());
		}
		// 数量が空欄の場合は0として処理し、小数3桁でHALF_UPにより四捨五入して保存する。
		// 読込後に変更した数量は、残数量を超える値・0・負数も保存できる。
		BigDecimal quantity = line.getQuantity() == null ? BigDecimal.ZERO : line.getQuantity();
		line.setQuantity(quantity.setScale(3, RoundingMode.HALF_UP));
	}

	// 取引先・担当者・倉庫の名称が空の場合は、同じIDの保存済み名称を保持し、なければ選択画面のデータから補完する。
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
