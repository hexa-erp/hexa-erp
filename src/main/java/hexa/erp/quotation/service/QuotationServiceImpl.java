package hexa.erp.quotation.service;

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
import hexa.erp.quotation.domain.QuotationCriteria;
import hexa.erp.quotation.domain.QuotationLineVO;
import hexa.erp.quotation.domain.QuotationVO;
import hexa.erp.quotation.mapper.QuotationMapper;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.warehouse.service.WarehouseLookupService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Service
public class QuotationServiceImpl implements QuotationService {

	@Setter(onMethod_ = @Autowired)
	private QuotationMapper mapper;

	@Setter(onMethod_ = @Autowired)
	private ItemLookupService itemLookupService;

	@Setter(onMethod_ = @Autowired)
	private PartnerLookupService partnerLookupService;

	@Setter(onMethod_ = @Autowired)
	private WarehouseLookupService warehouseLookupService;

	@Setter(onMethod_ = @Autowired)
	private AssigneeLookupService assigneeLookupService;

	@Override
	public QuotationVO get(Long quotationId) {
		log.info("見積一覧");

		if (quotationId == null) {
			return null;
		}

		QuotationVO quotation = mapper.read(quotationId);

		if (quotation != null)
			quotation.setLines(mapper.getLines(quotationId));

		return quotation;

	}

	@Override
	public List<QuotationVO> getList(QuotationCriteria criteria) {
		log.info("見積一覧を取得");

		return mapper.getListWithPaging(criteria);
	}

	@Override
	public int getTotal(QuotationCriteria criteria) {
		log.info("見積書の総件数を取得");

		return mapper.getTotalCount(criteria);
	}

	@Transactional
	@Override
	public Long save(QuotationVO quotation) {

		if (quotation == null || quotation.getBusinessDate() == null || quotation.getBusinessDate().trim().isEmpty()) {
			throw new IllegalArgumentException("見積書の入力内容を確認してください。");
		}

		try {
			quotation.setBusinessDate(LocalDate.parse(quotation.getBusinessDate().trim()).toString());

		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("正しい見積日を入力してください。");
		}

		List<QuotationLineVO> lines = new ArrayList<>();

		List<Long> removedLineIds = cleanIds(quotation.getRemovedLineIds());
		Set<Long> lineIds = new HashSet<>();

		// 重複した品目行や削除指定された品目行を検証する。
		if (quotation.getLines() != null) {

			for (QuotationLineVO line : quotation.getLines()) {

				if (line == null || line.getItemId() == null) {
					continue;
				}

				if (line.getQuotationLineId() != null) {
					if (!lineIds.add(line.getQuotationLineId()) || removedLineIds.contains(line.getQuotationLineId())) {
						throw new IllegalArgumentException("重複または削除指定された品目行を確認してください。");
					}
				}

				prepareLine(line);
				lines.add(line);
			}
		}

		if (lines.isEmpty()) {
			throw new IllegalArgumentException("見積の品目を1件以上入力してください。");
		}

		quotation.setLines(lines);

		// 新規見積書を登録する。
		if (quotation.getQuotationId() == null) {

			fillHeaderNames(quotation, null);

			mapper.insertSelectKey(quotation);

			for (QuotationLineVO line : lines) {

				line.setQuotationId(quotation.getQuotationId());

				mapper.insertLineSelectKey(line);
			}
		}

		// 既存の見積書を更新する。
		else {

			// 既存の見積書を読み込む。
			QuotationVO existing = mapper.read(quotation.getQuotationId());

			if (existing == null) {
				throw new IllegalArgumentException("更新対象の見積書が見つかりません。");
			}

			quotation.setQuotationNo(existing.getQuotationNo());
			quotation.setProgressStatus(existing.getProgressStatus());

			fillHeaderNames(quotation, existing);

			// 見積書のヘッダー情報を更新する。
			int result = mapper.update(quotation);

			if (result != 1) {
				throw new IllegalArgumentException("更新対象の見積書が見つかりません。");
			}

			// 既存の品目行を削除する。
			if (!cleanIds(quotation.getRemovedLineIds()).isEmpty()) {
				mapper.removeSelectedLines(quotation.getQuotationId(), quotation.getRemovedLineIds());
			}

			for (QuotationLineVO line : lines) {

				line.setQuotationId(quotation.getQuotationId());

				// 新しい品目行を登録する。
				if (line.getQuotationLineId() == null) {
					mapper.insertLineSelectKey(line);
				}

				// 既存の品目を更新する。
				else {

					int updateResult = mapper.updateLine(line);

					if (updateResult != 1) {
						throw new IllegalArgumentException("この見積書に属していないか、削除済みの品目行です。");
					}
				}

			}

		}

		log.info("見積書の保存: " + quotation.getQuotationId());
		return quotation.getQuotationId();
	}

	@Transactional
	@Override
	public int remove(List<Long> selectedIds) {

		List<Long> ids = cleanIds(selectedIds);

		if (ids.isEmpty()) {
			return 0;
		}

		int result = mapper.remove(ids);
		mapper.removeLines(ids);

		log.info("見積書の削除" + result + " 件");
		return result;

	}

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

		log.info("見積書の進行状態変更: " + progressStatus);
		return mapper.changeStatus(ids, progressStatus);
	}

	// 品目行を処理する。
	private void prepareLine(QuotationLineVO line) {

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

	// 見積書のヘッダーに保存する名前を補完する。
	private void fillHeaderNames(QuotationVO quotation, QuotationVO existing) {

		if (quotation.getPartnerId() == null) {
			quotation.setPartnerName(null);
		} else if (isBlank(quotation.getPartnerName())) {
			if (existing != null && quotation.getPartnerId().equals(existing.getPartnerId())
					&& !isBlank(existing.getPartnerName())) {
				quotation.setPartnerName(existing.getPartnerName());
			} else {
				PartnerLookupVO partner = partnerLookupService.get(quotation.getPartnerId());
				quotation.setPartnerName(partner == null ? null : partner.getPartnerName());
			}
		}

		if (quotation.getWarehouseId() == null) {
			quotation.setWarehouseName(null);
		} else if (isBlank(quotation.getWarehouseName())) {
			if (existing != null && quotation.getWarehouseId().equals(existing.getWarehouseId())
					&& !isBlank(existing.getWarehouseName())) {
				quotation.setWarehouseName(existing.getWarehouseName());
			} else {
				WarehouseLookupVO warehouse = warehouseLookupService.get(quotation.getWarehouseId());
				quotation.setWarehouseName(warehouse == null ? null : warehouse.getWarehouseName());
			}
		}

		if (quotation.getAssigneeId() == null) {
			quotation.setAssigneeName(null);
		} else if (isBlank(quotation.getAssigneeName())) {
			if (existing != null && quotation.getAssigneeId().equals(existing.getAssigneeId())
					&& !isBlank(existing.getAssigneeName())) {
				quotation.setAssigneeName(existing.getAssigneeName());
			} else {
				AssigneeLookupVO assignee = assigneeLookupService.get(quotation.getAssigneeId());
				quotation.setAssigneeName(assignee == null ? null : assignee.getAssigneeName());
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
