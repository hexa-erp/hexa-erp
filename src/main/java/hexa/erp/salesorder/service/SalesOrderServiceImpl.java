package hexa.erp.salesorder.service;

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
import hexa.erp.salesorder.domain.SalesOrderCriteria;
import hexa.erp.salesorder.domain.SalesOrderLineVO;
import hexa.erp.salesorder.domain.SalesOrderVO;
import hexa.erp.salesorder.mapper.SalesOrderMapper;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.warehouse.service.WarehouseLookupService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
public class SalesOrderServiceImpl implements SalesOrderService {

	@Setter(onMethod_ = @Autowired)
	private SalesOrderMapper mapper;

	@Setter(onMethod_ = @Autowired)
	private PartnerLookupService partnerLookupService;

	@Setter(onMethod_ = @Autowired)
	private WarehouseLookupService warehouseLookupService;

	@Setter(onMethod_ = @Autowired)
	private AssigneeLookupService assigneeLookupService;

	@Setter(onMethod_ = @Autowired)
	private ItemLookupService itemLookupService;

	@Override
	public List<SalesOrderVO> getList(SalesOrderCriteria criteria) {
		return mapper.getListWithPaging(criteria);
	}

	@Override
	public int getTotal(SalesOrderCriteria criteria) {
		return mapper.getTotalCount(criteria);
	}

	@Override
	public SalesOrderVO get(Long salesOrderId) {
		log.info("受注一覧");

		if (salesOrderId == null) {
			return null;
		}

		SalesOrderVO salesOrder = mapper.read(salesOrderId);
		if (salesOrder != null) {
			salesOrder.setLines(mapper.getLines(salesOrderId));
		}

		return salesOrder;
	}

	// 基本情報と品目行の保存は1つのトランザクションで処理し、途中で失敗した場合はすべてロールバックする。
	@Override
	@Transactional
	public Long save(SalesOrderVO salesOrder) {
		// 受注日付が空欄、または日付形式が不正な場合は保存せず、入力を確認する案内を返す。
		if (salesOrder == null || isBlank(salesOrder.getBusinessDate())) {
			throw new IllegalArgumentException("受注日を入力してください。");
		}

		try {
			salesOrder.setBusinessDate(LocalDate.parse(salesOrder.getBusinessDate().trim()).toString());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("正しい受注日を入力してください。");
		}

		// 納期は空欄ならNULLとし、指定された日付の形式が不正な場合はエラーとする。
		if (isBlank(salesOrder.getDueDate())) {
			salesOrder.setDueDate(null);
		} else {
			try {
				salesOrder.setDueDate(LocalDate.parse(salesOrder.getDueDate().trim()).toString());
			} catch (DateTimeParseException e) {
				throw new IllegalArgumentException("正しい納期を入力してください。");
			}
		}

		// 出荷倉庫が未選択の場合は保存できない。
		if (salesOrder.getWarehouseId() == null) {
			throw new IllegalArgumentException("出荷倉庫を選択してください。");
		}

		// 既存の受注伝票を読み込む。
		SalesOrderVO existing = null;
		if (salesOrder.getSalesOrderId() != null) {
			existing = mapper.read(salesOrder.getSalesOrderId());

			if (existing == null) {
				throw new IllegalArgumentException("更新対象の受注伝票が見つかりません。");
			}

			// 更新時は保存済みの番号・進行状態を保持する。
			salesOrder.setSalesOrderNo(existing.getSalesOrderNo());
			salesOrder.setProgressStatus(existing.getProgressStatus());
			;
		} else {
			// 新規伝票は進行中(IN_PROGRESS)で保存する。
			salesOrder.setProgressStatus("IN_PROGRESS");
		}

		fillHeaderNames(salesOrder, existing);
		if (isBlank(salesOrder.getWarehouseName())) {
			throw new IllegalArgumentException("選択した出荷倉庫が見つかりません。");
		}

		List<Long> removedLineIds = cleanIds(salesOrder.getRemovedLineIds());
		List<SalesOrderLineVO> lines = new ArrayList<>();
		Set<Long> lineIds = new HashSet<>();

		// 品目が未選択の空行は保存しない。明細IDの重複や削除リストとの重複はエラーとする。
		if (salesOrder.getLines() != null) {
			for (SalesOrderLineVO line : salesOrder.getLines()) {
				if (line == null || line.getItemId() == null) {
					continue;
				}
				if (line.getSalesOrderLineId() != null) {
					if (!lineIds.add(line.getSalesOrderLineId())
							|| removedLineIds.contains(line.getSalesOrderLineId())) {
						throw new IllegalArgumentException("重複または削除指定された品目行を確認してください。");
					}
				}
				prepareLine(line);
				lines.add(line);
			}
		}
		// 空行を除いた結果、残る行がない場合は品目を1件以上選択するよう案内する。
		if (lines.isEmpty()) {
			throw new IllegalArgumentException("受注品目を1件以上選択してください。");
		}
		salesOrder.setLines(lines);

		// 新規受注伝票を登録する。
		if (existing == null) {
			mapper.insertSelectKey(salesOrder);

			// 既存の受注伝票を更新する。
		} else if (mapper.update(salesOrder) != 1) {
			throw new IllegalArgumentException("更新対象の受注伝票が見つかりません。");
		}

		// 削除一覧の既存行に削除フラグを設定する。残る行は明細IDがなければ追加し、あれば更新する。
		if (!removedLineIds.isEmpty()) {
			mapper.removeSelectedLines(salesOrder.getSalesOrderId(), removedLineIds);
		}

		for (SalesOrderLineVO line : lines) {
			line.setSalesOrderId(salesOrder.getSalesOrderId());

			// 新規受注伝票の品目行を登録する。
			if (line.getSalesOrderLineId() == null) {
				mapper.insertLineSelectKey(line);

				// 既存の受注伝票の品目行を更新する。
			} else if (mapper.updateLine(line) != 1) {
				throw new IllegalArgumentException("この受注伝票に属していないか、削除済みの品目行です。");
			}
		}

		log.info("受注伝票の保存: " + salesOrder.getSalesOrderId());
		return salesOrder.getSalesOrderId();
	}

	// チェックした受注書の基本情報と品目行に削除フラグを設定する。行は物理削除しない。
	@Override
	@Transactional
	public int remove(List<Long> selectedIds) {
		List<Long> ids = cleanIds(selectedIds);
		if (ids.isEmpty()) {
			return 0;
		}

		// 受注伝票のヘッダー情報を削除する。
		int count = mapper.remove(ids);

		// 受注伝票の品目行を削除する。
		mapper.removeLines(ids);

		log.info("受注伝票の削除: " + count + "件");

		return count;
	}

	// 選択した受注書を未確認・進行中・完了のいずれかに変更し、許可されていない状態は拒否する。
	@Override
	public int changeStatus(List<Long> selectedIds, String progressStatus) {
		List<Long> ids = cleanIds(selectedIds);
		if (ids.isEmpty()) {
			return 0;
		}

		if (!"UNCONFIRMED".equals(progressStatus) && !"IN_PROGRESS".equals(progressStatus)
				&& !"COMPLETED".equals(progressStatus)) {
			throw new IllegalArgumentException("進行状態は未確認、進行中、完了のいずれかを選択してください。");
		}

		log.info("受注伝票の進行状態変更: " + progressStatus);
		return mapper.changeStatus(ids, progressStatus);
	}

	// 品目名称が空の場合は選択画面のデータから補完し、数量・単価から保存する金額を計算する。
	// 見積書から読込後に入力した数量は、元の残数量を超えていても保存対象とする。
	private void prepareLine(SalesOrderLineVO line) {
		if (isBlank(line.getItemName())) {
			ItemLookupVO item = itemLookupService.get(line.getItemId());

			if (item == null) {
				throw new IllegalArgumentException("選択した品目が見つかりません。");
			}

			line.setItemName(item.getItemName());
		}

		BigDecimal quantity = line.getQuantity() == null ? BigDecimal.ZERO : line.getQuantity();
		BigDecimal unitPrice = line.getUnitPrice() == null ? BigDecimal.ZERO : line.getUnitPrice();

		// 数量は小数第4位を四捨五入して小数第3位まで、単価は小数第3位を四捨五入して小数第2位までとする。
		quantity = quantity.setScale(3, RoundingMode.HALF_UP);
		unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);

		// 供給価額（数量×単価）はウォン単位で四捨五入する。
		BigDecimal supplyAmount = quantity.multiply(unitPrice);
		supplyAmount = supplyAmount.setScale(0, RoundingMode.HALF_UP);

		// 付加価値税（供給価額×10%）はウォン単位で四捨五入する。
		BigDecimal vatAmount = supplyAmount.multiply(new BigDecimal("0.1"));
		vatAmount = vatAmount.setScale(0, RoundingMode.HALF_UP);

		// 合計を計算する。
		BigDecimal totalAmount = supplyAmount.add(vatAmount);

		line.setQuantity(quantity);
		line.setUnitPrice(unitPrice);
		line.setSupplyAmount(supplyAmount);
		line.setVatAmount(vatAmount);
		line.setTotalAmount(totalAmount);
	}

	// 取引先・担当者・倉庫の名称が空の場合は、既存伝票または選択画面のデータから補完する。
	private void fillHeaderNames(SalesOrderVO salesOrder, SalesOrderVO existing) {
		if (salesOrder.getPartnerId() == null) {
			salesOrder.setPartnerName(null);
		} else if (isBlank(salesOrder.getPartnerName())) {
			if (existing != null && salesOrder.getPartnerId().equals(existing.getPartnerId())
					&& !isBlank(existing.getPartnerName())) {
				salesOrder.setPartnerName(existing.getPartnerName());
			} else {
				PartnerLookupVO partner = partnerLookupService.get(salesOrder.getPartnerId());
				salesOrder.setPartnerName(partner == null ? null : partner.getPartnerName());
			}
		}

		if (salesOrder.getWarehouseId() == null) {
			salesOrder.setWarehouseName(null);
		} else if (isBlank(salesOrder.getWarehouseName())) {
			if (existing != null && salesOrder.getWarehouseId().equals(existing.getWarehouseId())
					&& !isBlank(existing.getWarehouseName())) {
				salesOrder.setWarehouseName(existing.getWarehouseName());
			} else {
				WarehouseLookupVO warehouse = warehouseLookupService.get(salesOrder.getWarehouseId());
				salesOrder.setWarehouseName(warehouse == null ? null : warehouse.getWarehouseName());
			}
		}

		if (salesOrder.getAssigneeId() == null) {
			salesOrder.setAssigneeName(null);
		} else if (isBlank(salesOrder.getAssigneeName())) {
			if (existing != null && salesOrder.getAssigneeId().equals(existing.getAssigneeId())
					&& !isBlank(existing.getAssigneeName())) {
				salesOrder.setAssigneeName(existing.getAssigneeName());
			} else {
				AssigneeLookupVO assignee = assigneeLookupService.get(salesOrder.getAssigneeId());
				salesOrder.setAssigneeName(assignee == null ? null : assignee.getAssigneeName());
			}
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	// 選択した受注伝票の品目行IDの重複を除去する。
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
