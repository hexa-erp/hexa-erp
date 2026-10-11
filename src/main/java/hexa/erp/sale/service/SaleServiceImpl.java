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

    // 판매 한 건 조회
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

    // 판매 저장
    @Override
    @Transactional
    public Long save(SaleVO sale) {
        if (sale == null || isBlank(sale.getBusinessDate())) {
            throw new IllegalArgumentException(
                "판매 일자를 입력해 주세요."
            );
        }

        try {
            sale.setBusinessDate(
                LocalDate.parse(
                    sale.getBusinessDate().trim()
                ).toString()
            );
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "판매 일자를 올바르게 입력해 주세요."
            );
        }

        if (sale.getWarehouseId() == null) {
            throw new IllegalArgumentException(
                "출하창고를 선택해 주세요."
            );
        }

        SaleVO existing = null;
        List<SaleLineVO> previousLines =
            Collections.emptyList();

        if (sale.getSaleId() != null) {
            existing = mapper.read(sale.getSaleId());

            if (existing == null) {
                throw new IllegalArgumentException(
                    "수정할 판매 전표를 찾을 수 없습니다."
                );
            }

            sale.setSaleNo(existing.getSaleNo());
            sale.setProgressStatus(
                existing.getProgressStatus()
            );

            previousLines =
                mapper.getLines(sale.getSaleId());
        } else {
            sale.setProgressStatus("CONFIRMED");
        }

        fillHeaderNames(sale, existing);

        if (isBlank(sale.getWarehouseName())) {
            throw new IllegalArgumentException(
                "선택한 출하창고를 찾을 수 없습니다."
            );
        }

        List<Long> removedLineIds =
            cleanIds(sale.getRemovedLineIds());

        List<SaleLineVO> lines = new ArrayList<>();
        Set<Long> lineIds = new HashSet<>();

        if (sale.getLines() != null) {
            for (SaleLineVO line : sale.getLines()) {
                if (line == null || line.getItemId() == null) {
                    continue;
                }

                if (line.getSaleLineId() != null) {
                    boolean duplicated =
                        !lineIds.add(line.getSaleLineId());

                    boolean removed =
                        removedLineIds.contains(
                            line.getSaleLineId()
                        );

                    if (duplicated || removed) {
                        throw new IllegalArgumentException(
                            "중복되거나 삭제 표시된 품목 행을 확인해 주세요."
                        );
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
            throw new IllegalArgumentException(
                "수정할 판매 전표를 찾을 수 없습니다."
            );
        }

        if (!removedLineIds.isEmpty()) {
            mapper.removeSelectedLines(
                sale.getSaleId(),
                removedLineIds
            );
        }

        for (SaleLineVO line : lines) {
            line.setSaleId(sale.getSaleId());

            if (line.getSaleLineId() == null) {
                mapper.insertLineSelectKey(line);
            } else if (mapper.updateLine(line) != 1) {
                throw new IllegalArgumentException(
                    "판매 전표에 속하지 않거나 삭제된 품목 행입니다."
                );
            }
        }

        List<SaleLineVO> savedLines =
            mapper.getLines(sale.getSaleId());

        if (savedLines.isEmpty()) {
            throw new IllegalArgumentException(
                "판매 품목을 한 개 이상 선택해 주세요."
            );
        }

        applyStockChanges(
            existing,
            previousLines,
            sale,
            savedLines
        );

        sale.setLines(savedLines);

        log.info(
            "판매 전표 저장: "
                + sale.getSaleId()
        );

        return sale.getSaleId();
    }

    // 판매 수정에 따른 재고 반영
    private void applyStockChanges(
            SaleVO existing,
            List<SaleLineVO> previousLines,
            SaleVO sale,
            List<SaleLineVO> savedLines) {

        Map<Long, SaleLineVO> previousById =
            new LinkedHashMap<>();

        for (SaleLineVO line : previousLines) {
            previousById.put(
                line.getSaleLineId(),
                line
            );
        }

        for (SaleLineVO line : savedLines) {
            SaleLineVO previous =
                previousById.remove(
                    line.getSaleLineId()
                );

            if (previous == null) {
                applyStockDelta(
                    sale.getSaleId(),
                    line,
                    sale.getWarehouseId(),
                    line.getQuantity().negate()
                );
            } else if (
                existing.getWarehouseId().equals(
                    sale.getWarehouseId()
                )
                && previous.getItemId().equals(
                    line.getItemId()
                )
            ) {
                applyStockDelta(
                    sale.getSaleId(),
                    line,
                    sale.getWarehouseId(),
                    previous.getQuantity().subtract(
                        line.getQuantity()
                    )
                );
            } else {
                applyStockDelta(
                    sale.getSaleId(),
                    previous,
                    existing.getWarehouseId(),
                    previous.getQuantity()
                );

                applyStockDelta(
                    sale.getSaleId(),
                    line,
                    sale.getWarehouseId(),
                    line.getQuantity().negate()
                );
            }
        }

        for (SaleLineVO removed
                : previousById.values()) {

            applyStockDelta(
                sale.getSaleId(),
                removed,
                existing.getWarehouseId(),
                removed.getQuantity()
            );
        }
    }

    // 재고 증감 요청
    private void applyStockDelta(
            Long saleId,
            SaleLineVO line,
            Long warehouseId,
            BigDecimal quantityDelta) {

        if (quantityDelta.compareTo(
                BigDecimal.ZERO) == 0) {

            return;
        }

        StockMovementVO movement =
            new StockMovementVO();

        movement.setSaleId(saleId);
        movement.setSaleLineId(
            line.getSaleLineId()
        );
        movement.setWarehouseId(warehouseId);
        movement.setItemId(line.getItemId());
        movement.setQuantityDelta(quantityDelta);

        stockService.applySaleMovement(movement);
    }

    // 판매 상세 금액 계산
    private void prepareLine(SaleLineVO line) {
        if (isBlank(line.getItemName())) {
            ItemLookupVO item =
                itemLookupService.get(
                    line.getItemId()
                );

            if (item == null) {
                throw new IllegalArgumentException(
                    "선택한 품목을 찾을 수 없습니다."
                );
            }

            line.setItemName(item.getItemName());
        }

        BigDecimal quantity =
            line.getQuantity() == null
                ? BigDecimal.ZERO
                : line.getQuantity();

        BigDecimal unitPrice =
            line.getUnitPrice() == null
                ? BigDecimal.ZERO
                : line.getUnitPrice();

        quantity = quantity.setScale(
            3,
            RoundingMode.HALF_UP
        );

        unitPrice = unitPrice.setScale(
            2,
            RoundingMode.HALF_UP
        );

        BigDecimal supplyAmount =
            quantity.multiply(unitPrice).setScale(
                0,
                RoundingMode.HALF_UP
            );

        BigDecimal vatAmount =
            supplyAmount.multiply(
                new BigDecimal("0.1")
            ).setScale(
                0,
                RoundingMode.HALF_UP
            );

        line.setQuantity(quantity);
        line.setUnitPrice(unitPrice);
        line.setSupplyAmount(supplyAmount);
        line.setVatAmount(vatAmount);
        line.setTotalAmount(
            supplyAmount.add(vatAmount)
        );
    }

    // 거래처와 창고 및 담당자명 보완
    private void fillHeaderNames(
            SaleVO sale,
            SaleVO existing) {

        if (sale.getPartnerId() == null) {
            sale.setPartnerName(null);
        } else if (isBlank(sale.getPartnerName())) {
            if (
                existing != null
                && sale.getPartnerId().equals(
                    existing.getPartnerId()
                )
                && !isBlank(
                    existing.getPartnerName()
                )
            ) {
                sale.setPartnerName(
                    existing.getPartnerName()
                );
            } else {
                PartnerLookupVO partner =
                    partnerLookupService.get(
                        sale.getPartnerId()
                    );

                sale.setPartnerName(
                    partner == null
                        ? null
                        : partner.getPartnerName()
                );
            }
        }

        if (sale.getWarehouseId() == null) {
            sale.setWarehouseName(null);
        } else if (isBlank(
                sale.getWarehouseName())) {

            if (
                existing != null
                && sale.getWarehouseId().equals(
                    existing.getWarehouseId()
                )
                && !isBlank(
                    existing.getWarehouseName()
                )
            ) {
                sale.setWarehouseName(
                    existing.getWarehouseName()
                );
            } else {
                WarehouseLookupVO warehouse =
                    warehouseLookupService.get(
                        sale.getWarehouseId()
                    );

                sale.setWarehouseName(
                    warehouse == null
                        ? null
                        : warehouse.getWarehouseName()
                );
            }
        }

        if (sale.getAssigneeId() == null) {
            sale.setAssigneeName(null);
        } else if (isBlank(
                sale.getAssigneeName())) {

            if (
                existing != null
                && sale.getAssigneeId().equals(
                    existing.getAssigneeId()
                )
                && !isBlank(
                    existing.getAssigneeName()
                )
            ) {
                sale.setAssigneeName(
                    existing.getAssigneeName()
                );
            } else {
                AssigneeLookupVO assignee =
                    assigneeLookupService.get(
                        sale.getAssigneeId()
                    );

                sale.setAssigneeName(
                    assignee == null
                        ? null
                        : assignee.getAssigneeName()
                );
            }
        }
    }

    // 빈 문자열 확인
    private boolean isBlank(String value) {
        return value == null
            || value.trim().isEmpty();
    }

    // 중복 ID 제거
    private List<Long> cleanIds(
            List<Long> values) {

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
