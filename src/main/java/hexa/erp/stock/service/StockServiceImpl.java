package hexa.erp.stock.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hexa.erp.stock.domain.StockMovementVO;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.stock.mapper.StockMapper;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    // 재고 Mapper 연결
    private final StockMapper mapper;

    // 전체 현재고 조회
    @Override
    public List<StockVO> getList() {
        return mapper.getList();
    }

    // 사용 중인 창고 조회
    @Override
    public List<WarehouseLookupVO> getWarehouseList() {
        return mapper.getWarehouseList();
    }

    // 현재고와 변동 이력 저장
    @Override
    @Transactional
    public void adjust(StockVO stock) {
        if (stock == null
                || stock.getWarehouseId() == null
                || stock.getItemId() == null
                || stock.getQuantity() == null) {

            throw new IllegalArgumentException(
                "창고, 품목, 조정 후 재고수량을 입력해 주세요."
            );
        }

        BigDecimal targetQuantity =
            stock.getQuantity().setScale(
                3,
                RoundingMode.HALF_UP
            );

        StockVO current = mapper.read(
            stock.getWarehouseId(),
            stock.getItemId()
        );

        if (current == null) {
            current = new StockVO();
            current.setWarehouseId(stock.getWarehouseId());
            current.setItemId(stock.getItemId());
            current.setQuantity(BigDecimal.ZERO);

            mapper.insert(current);
        }

        BigDecimal quantityDelta =
            targetQuantity.subtract(
                current.getQuantity()
            );

        stock.setQuantity(targetQuantity);
        mapper.update(stock);

        if (quantityDelta.compareTo(BigDecimal.ZERO) != 0) {
            StockMovementVO movement =
                new StockMovementVO();

            movement.setWarehouseId(
                stock.getWarehouseId()
            );
            movement.setItemId(stock.getItemId());
            movement.setQuantityDelta(quantityDelta);

            mapper.insertMovement(movement);
        }

        log.info(
            "재고 조정: 창고 "
                + stock.getWarehouseId()
                + ", 품목 "
                + stock.getItemId()
                + ", 조정 후 수량 "
                + targetQuantity
        );
    }
}
