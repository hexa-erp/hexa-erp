package hexa.erp.sale.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hexa.erp.assignee.domain.AssigneeLookupVO;
import hexa.erp.assignee.service.AssigneeLookupService;
import hexa.erp.item.domain.ItemLookupVO;
import hexa.erp.item.service.ItemLookupService;
import hexa.erp.partner.domain.PartnerLookupVO;
import hexa.erp.partner.service.PartnerLookupService;
import hexa.erp.sale.domain.SaleCriteria;
import hexa.erp.sale.domain.SaleLineVO;
import hexa.erp.sale.domain.SaleVO;
import hexa.erp.sale.mapper.SaleMapper;
import hexa.erp.stock.domain.StockMovementVO;
import hexa.erp.stock.service.StockService;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.warehouse.service.WarehouseLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

	private final SaleMapper mapper;

	private final PartnerLookupService partnerLookupService;

	private final WarehouseLookupService warehouseLookupService;

	private final AssigneeLookupService assigneeLookupService;

	private final ItemLookupService itemLookupService;

	private final StockService stockService;

	// 売上一覧を取得する。
	@Override
	public List<SaleVO> getList(SaleCriteria criteria) {

		if (criteria == null) {
			criteria = new SaleCriteria();
		}

		return mapper.getListWithPaging(criteria);
	}

	// 売上の総件数を取得する。
	@Override
	public int getTotal(SaleCriteria criteria) {

		if (criteria == null) {
			criteria = new SaleCriteria();
		}

		return mapper.getTotalCount(criteria);
	}

	// 売上を1件取得する。
	@Override
	public SaleVO get(Long saleId) {
		if (saleId == null) {
			return null;
		}

		SaleVO sale = mapper.read(saleId);

		if (sale != null) {
			sale.setLines(mapper.getLines(saleId));
		}

		return sale;
	}

	// 売上を保存する。
	@Override
	@Transactional
	public Long save(SaleVO sale) {
		if (sale == null || isBlank(sale.getBusinessDate())) {
			throw new IllegalArgumentException("売上日を入力してください。");
		}

		try {
			sale.setBusinessDate(LocalDate.parse(sale.getBusinessDate().trim()).toString());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("正しい売上日を入力してください。");
		}

		if (sale.getWarehouseId() == null) {
			throw new IllegalArgumentException("出荷倉庫を選択してください。");
		}

		SaleVO existing = null;

		List<SaleLineVO> previousLines = Collections.emptyList();

		if (sale.getSaleId() != null) {
			existing = mapper.read(sale.getSaleId());

			if (existing == null) {
				throw new IllegalArgumentException("更新対象の売上伝票が見つかりません。");
			}

			sale.setSaleNo(existing.getSaleNo());

			sale.setProgressStatus(existing.getProgressStatus());

			previousLines = mapper.getLines(sale.getSaleId());
		} else {
			sale.setProgressStatus("CONFIRMED");
		}

		fillHeaderNames(sale, existing);

		if (isBlank(sale.getWarehouseName())) {

			throw new IllegalArgumentException("選択した出荷倉庫が見つかりません。");
		}

		List<Long> removedLineIds = cleanIds(sale.getRemovedLineIds());

		List<SaleLineVO> lines = new ArrayList<>();

		Set<Long> lineIds = new HashSet<>();

		if (sale.getLines() != null) {
			for (SaleLineVO line : sale.getLines()) {
				if (line == null || line.getItemId() == null) {
					continue;
				}

				if (line.getSaleLineId() != null) {
					boolean duplicated = !lineIds.add(line.getSaleLineId());

					boolean removed = removedLineIds.contains(line.getSaleLineId());

					if (duplicated || removed) {
						throw new IllegalArgumentException("重複または削除指定された品目行を確認してください。");
					}
				}

				prepareLine(line);
				lines.add(line);
			}
		}

		sale.setLines(lines);

		if (existing == null) {
			mapper.insertSelectKey(sale);
		} else if (mapper.update(sale) != 1) {
			throw new IllegalArgumentException("更新対象の売上伝票が見つかりません。");
		}

		if (!removedLineIds.isEmpty()) {
			mapper.removeSelectedLines(sale.getSaleId(), removedLineIds);
		}

		for (SaleLineVO line : lines) {
			line.setSaleId(sale.getSaleId());

			if (line.getSaleLineId() == null) {
				mapper.insertLineSelectKey(line);
			} else if (mapper.updateLine(line) != 1) {
				throw new IllegalArgumentException("この売上伝票に属していないか、削除済みの品目行です。");
			}
		}

		List<SaleLineVO> savedLines = mapper.getLines(sale.getSaleId());

		if (savedLines.isEmpty()) {
			throw new IllegalArgumentException("売上品目を1件以上選択してください。");
		}

		applyStockChanges(existing, previousLines, sale, savedLines);

		sale.setLines(savedLines);

		log.info("売上伝票の保存: " + sale.getSaleId());

		return sale.getSaleId();
	}

	// 選択した売上を削除する。
	@Override
	@Transactional
	public int remove(List<Long> selectedIds) {

		List<Long> ids = cleanIds(selectedIds);

		if (ids.isEmpty()) {
			return 0;
		}

		int count = 0;

		for (Long saleId : ids) {
			SaleVO sale = mapper.read(saleId);

			if (sale == null) {
				continue;
			}

			List<SaleLineVO> lines = mapper.getLines(saleId);

			List<Long> oneId = Collections.singletonList(saleId);

			int removeCount = mapper.remove(oneId);

			if (removeCount != 1) {
				continue;
			}

			mapper.removeLines(oneId);

			for (SaleLineVO line : lines) {
				applyStockDelta(saleId, line, sale.getWarehouseId(), line.getQuantity());
			}

			count++;
		}

		log.info("売上伝票の削除: " + count + "件");

		return count;
	}

	// 選択した売上の進行状態を変更する。
	@Override
	public int changeStatus(List<Long> selectedIds, String progressStatus) {

		List<Long> ids = cleanIds(selectedIds);

		if (ids.isEmpty()) {
			return 0;
		}

		if (!"UNCONFIRMED".equals(progressStatus) && !"CONFIRMED".equals(progressStatus)) {
			throw new IllegalArgumentException("進行状態は未確認、確認のいずれかを選択してください。");
		}

		log.info("売上伝票の進行状態変更: " + progressStatus);

		return mapper.changeStatus(ids, progressStatus);
	}

	// 売上の更新内容を在庫に反映する。
	private void applyStockChanges(SaleVO existing, List<SaleLineVO> previousLines, SaleVO sale,
			List<SaleLineVO> savedLines) {

		Map<Long, SaleLineVO> previousById = new LinkedHashMap<>();

		for (SaleLineVO line : previousLines) {
			previousById.put(line.getSaleLineId(), line);
		}

		for (SaleLineVO line : savedLines) {
			SaleLineVO previous = previousById.remove(line.getSaleLineId());

			if (previous == null) {
				applyStockDelta(sale.getSaleId(), line, sale.getWarehouseId(), line.getQuantity().negate());
			} else if (existing.getWarehouseId().equals(sale.getWarehouseId())
					&& previous.getItemId().equals(line.getItemId())) {
				applyStockDelta(sale.getSaleId(), line, sale.getWarehouseId(),
						previous.getQuantity().subtract(line.getQuantity()));
			} else {
				applyStockDelta(sale.getSaleId(), previous, existing.getWarehouseId(), previous.getQuantity());

				applyStockDelta(sale.getSaleId(), line, sale.getWarehouseId(), line.getQuantity().negate());
			}
		}

		for (SaleLineVO removed : previousById.values()) {
			applyStockDelta(sale.getSaleId(), removed, existing.getWarehouseId(), removed.getQuantity());
		}
	}

	// 在庫増減のリクエスト
	private void applyStockDelta(Long saleId, SaleLineVO line, Long warehouseId, BigDecimal quantityDelta) {

		if (quantityDelta.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}

		StockMovementVO movement = new StockMovementVO();

		movement.setSaleId(saleId);
		movement.setSaleLineId(line.getSaleLineId());
		movement.setWarehouseId(warehouseId);
		movement.setItemId(line.getItemId());
		movement.setQuantityDelta(quantityDelta);

		stockService.applySaleMovement(movement);
	}

	// 売上明細の金額を計算する。
	private void prepareLine(SaleLineVO line) {

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

		BigDecimal supplyAmount = quantity.multiply(unitPrice).setScale(0, RoundingMode.HALF_UP);

		BigDecimal vatAmount = supplyAmount.multiply(new BigDecimal("0.1")).setScale(0, RoundingMode.HALF_UP);

		line.setQuantity(quantity);
		line.setUnitPrice(unitPrice);
		line.setSupplyAmount(supplyAmount);
		line.setVatAmount(vatAmount);
		line.setTotalAmount(supplyAmount.add(vatAmount));
	}

	// 取引先名・倉庫名・担当者名を補完する。
	private void fillHeaderNames(SaleVO sale, SaleVO existing) {

		if (sale.getPartnerId() == null) {
			sale.setPartnerName(null);
		} else if (isBlank(sale.getPartnerName())) {
			if (existing != null && sale.getPartnerId().equals(existing.getPartnerId())
					&& !isBlank(existing.getPartnerName())) {
				sale.setPartnerName(existing.getPartnerName());
			} else {
				PartnerLookupVO partner = partnerLookupService.get(sale.getPartnerId());

				sale.setPartnerName(partner == null ? null : partner.getPartnerName());
			}
		}

		if (sale.getWarehouseId() == null) {
			sale.setWarehouseName(null);
		} else if (isBlank(sale.getWarehouseName())) {
			if (existing != null && sale.getWarehouseId().equals(existing.getWarehouseId())
					&& !isBlank(existing.getWarehouseName())) {
				sale.setWarehouseName(existing.getWarehouseName());
			} else {
				WarehouseLookupVO warehouse = warehouseLookupService.get(sale.getWarehouseId());

				sale.setWarehouseName(warehouse == null ? null : warehouse.getWarehouseName());
			}
		}

		if (sale.getAssigneeId() == null) {
			sale.setAssigneeName(null);
		} else if (isBlank(sale.getAssigneeName())) {
			if (existing != null && sale.getAssigneeId().equals(existing.getAssigneeId())
					&& !isBlank(existing.getAssigneeName())) {
				sale.setAssigneeName(existing.getAssigneeName());
			} else {
				AssigneeLookupVO assignee = assigneeLookupService.get(sale.getAssigneeId());

				sale.setAssigneeName(assignee == null ? null : assignee.getAssigneeName());
			}
		}
	}

	// 空文字かどうかを確認する。
	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	// 重複IDを除去する。
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
