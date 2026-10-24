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
		log.info("주문서 조회");

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
			throw new IllegalArgumentException("주문 일자를 입력하세요.");
		}

		try {
			salesOrder.setBusinessDate(LocalDate.parse(salesOrder.getBusinessDate().trim()).toString());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("주문 일자를 올바르게 입력하세요.");
		}

		if (isBlank(salesOrder.getDueDate())) {
			salesOrder.setDueDate(null);
		} else {
			try {
				salesOrder.setDueDate(LocalDate.parse(salesOrder.getDueDate().trim()).toString());
			} catch (DateTimeParseException e) {
				throw new IllegalArgumentException("납기 일자를 올바르게 입력하세요.");
			}
		}

		if (salesOrder.getWarehouseId() == null) {
			throw new IllegalArgumentException("출하창고를 선택해 주세요.");
		}

		// 기존 주문서 불러오기
		SalesOrderVO existing = null;
		if (salesOrder.getSalesOrderId() != null) {
			existing = mapper.read(salesOrder.getSalesOrderId());

			if (existing == null) {
				throw new IllegalArgumentException("수정할 주문서를 찾을 수 없습니다.");
			}

			salesOrder.setSalesOrderNo(existing.getSalesOrderNo());
			salesOrder.setProgressStatus(existing.getProgressStatus());
			;
		} else {
			salesOrder.setProgressStatus("IN_PROGRESS");
		}

		fillHeaderNames(salesOrder, existing);
		if (isBlank(salesOrder.getWarehouseName())) {
			throw new IllegalArgumentException("선택한 출하창고를 찾을 수 없습니다.");
		}

		List<Long> removedLineIds = cleanIds(salesOrder.getRemovedLineIds());
		List<SalesOrderLineVO> lines = new ArrayList<>();
		Set<Long> lineIds = new HashSet<>();

		// 중복, 삭제된 품목 행 검사
		if (salesOrder.getLines() != null) {
			for (SalesOrderLineVO line : salesOrder.getLines()) {
				if (line == null || line.getItemId() == null) {
					continue;
				}
				if (line.getSalesOrderLineId() != null) {
					if (!lineIds.add(line.getSalesOrderLineId())
							|| removedLineIds.contains(line.getSalesOrderLineId())) {
						throw new IllegalArgumentException("중복되거나 삭제 표시된 품목 행을 확인해 주세요.");
					}
				}
				prepareLine(line);
				lines.add(line);
			}
		}
		if (lines.isEmpty()) {
			throw new IllegalArgumentException("주문 품목을 한 개 이상 선택해 주세요");
		}
		salesOrder.setLines(lines);

		// 신규 주문서 등록
		if (existing == null) {
			mapper.insertSelectKey(salesOrder);

			// 기존 주문서 수정
		} else if (mapper.update(salesOrder) != 1) {
			throw new IllegalArgumentException("수정할 주문서를 찾을 수 없습니다.");
		}

		if (!removedLineIds.isEmpty()) {
			mapper.removeSelectedLines(salesOrder.getSalesOrderId(), removedLineIds);
		}

		for (SalesOrderLineVO line : lines) {
			line.setSalesOrderId(salesOrder.getSalesOrderId());

			// 신규 주문서 품목 행 입력
			if (line.getSalesOrderLineId() == null) {
				mapper.insertLineSelectKey(line);

				// 기존 주문서 품목 행 수정
			} else if (mapper.updateLine(line) != 1) {
				throw new IllegalArgumentException("주문서에 속하지 않거나 삭제된 품목 행 입니다.");
			}
		}

		log.info("주문서 저장: " + salesOrder.getSalesOrderId());
		return salesOrder.getSalesOrderId();
	}

	// 주문서 삭제
	@Override
	@Transactional
	public int remove(List<Long> selectedIds) {
		List<Long> ids = cleanIds(selectedIds);
		if (ids.isEmpty()) {
			return 0;
		}

		// 주문서 기본 정보 삭제
		int count = mapper.remove(ids);

		// 주문서 품목 행 삭제
		mapper.removeLines(ids);

		log.info("주문서 삭제: " + count + "건");

		return count;
	}

	// 주문서 진행 상태 변경
	@Override
	public int changeStatus(List<Long> selectedIds, String progressStatus) {
		List<Long> ids = cleanIds(selectedIds);
		if (ids.isEmpty()) {
			return 0;
		}

		if (!"UNCONFIRMED".equals(progressStatus) && !"IN_PROGRESS".equals(progressStatus)
				&& !"COMPLETED".equals(progressStatus)) {
			throw new IllegalArgumentException("진행상태는 미확인, 진행중, 완료 중에서 선택해 주세요.");
		}

		log.info("주문서 진행상태 변경: " + progressStatus);
		return mapper.changeStatus(ids, progressStatus);
	}

	// 품목행 값 계산
	private void prepareLine(SalesOrderLineVO line) {
		if (isBlank(line.getItemName())) {
			ItemLookupVO item = itemLookupService.get(line.getItemId());

			if (item == null) {
				throw new IllegalArgumentException("선택한 품목을 찾을 수 없습니다.");
			}

			line.setItemName(item.getItemName());
		}

		BigDecimal quantity = line.getQuantity() == null ? BigDecimal.ZERO : line.getQuantity();
		BigDecimal unitPrice = line.getUnitPrice() == null ? BigDecimal.ZERO : line.getUnitPrice();

		quantity = quantity.setScale(3, RoundingMode.HALF_UP);
		unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);

		// 공급가액 계산
		BigDecimal supplyAmount = quantity.multiply(unitPrice);
		supplyAmount = supplyAmount.setScale(0, RoundingMode.HALF_UP);

		// 부가세 계산
		BigDecimal vatAmount = supplyAmount.multiply(new BigDecimal("0.1"));
		vatAmount = vatAmount.setScale(0, RoundingMode.HALF_UP);

		// 합계 계산
		BigDecimal totalAmount = supplyAmount.add(vatAmount);

		line.setQuantity(quantity);
		line.setUnitPrice(unitPrice);
		line.setSupplyAmount(supplyAmount);
		line.setVatAmount(vatAmount);
		line.setTotalAmount(totalAmount);
	}

	// 주문서 기본정보에 들어가는 이름 보완
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

	// 선택한 주문서 품목행 id 중복 제거
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
