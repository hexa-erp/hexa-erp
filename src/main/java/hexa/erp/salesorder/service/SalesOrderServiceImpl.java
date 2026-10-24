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

	@Override
	@Transactional
	public Long save(SalesOrderVO salesOrder) {
		if (salesOrder == null || isBlank(salesOrder.getBusinessDate())) {
			throw new IllegalArgumentException("受注日を入力してください。");
		}

		try {
			salesOrder.setBusinessDate(LocalDate.parse(salesOrder.getBusinessDate().trim()).toString());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("正しい受注日を入力してください。");
		}

		if (isBlank(salesOrder.getDueDate())) {
			salesOrder.setDueDate(null);
		} else {
			try {
				salesOrder.setDueDate(LocalDate.parse(salesOrder.getDueDate().trim()).toString());
			} catch (DateTimeParseException e) {
				throw new IllegalArgumentException("正しい納期を入力してください。");
			}
		}

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

			salesOrder.setSalesOrderNo(existing.getSalesOrderNo());
			salesOrder.setProgressStatus(existing.getProgressStatus());
			;
		} else {
			salesOrder.setProgressStatus("IN_PROGRESS");
		}

		fillHeaderNames(salesOrder, existing);
		if (isBlank(salesOrder.getWarehouseName())) {
			throw new IllegalArgumentException("選択した出荷倉庫が見つかりません。");
		}

		List<Long> removedLineIds = cleanIds(salesOrder.getRemovedLineIds());
		List<SalesOrderLineVO> lines = new ArrayList<>();
		Set<Long> lineIds = new HashSet<>();

		// 重複した品目行や削除指定された品目行を検証する。
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

	// 受注伝票を削除する。
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

	// 受注伝票の進行状態を変更する。
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

	// 品目行の値を計算する。
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

		quantity = quantity.setScale(3, RoundingMode.HALF_UP);
		unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);

		// 税抜金額を計算する。
		BigDecimal supplyAmount = quantity.multiply(unitPrice);
		supplyAmount = supplyAmount.setScale(0, RoundingMode.HALF_UP);

		// 消費税を計算する。
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

	// 受注伝票のヘッダーに保存する名前を補完する。
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
