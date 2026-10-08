package hexa.erp.stock.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import hexa.erp.item.domain.ItemVO;
import hexa.erp.item.mapper.ItemMapper;
import hexa.erp.stock.domain.StockMovementVO;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    "file:src/main/webapp/WEB-INF/spring/root-context.xml"
)
@Transactional
@Rollback
@Log4j
public class StockMapperTests {

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private ItemMapper itemMapper;

    // Mapper 객체 주입 확인
    @Test
    public void testMapperExist() {
        log.info("========== MAPPER INJECTION TEST ==========");
        log.info("STOCK MAPPER: " + stockMapper);
        log.info("ITEM MAPPER: " + itemMapper);

        assertNotNull(stockMapper);
        assertNotNull(itemMapper);
    }

    // 사용 중인 창고 조회 확인
    @Test
    public void testGetWarehouseList() {
        List<WarehouseLookupVO> warehouseList =
            stockMapper.getWarehouseList();

        log.info("========== WAREHOUSE LIST TEST ==========");
        log.info("WAREHOUSE COUNT: " + warehouseList.size());
        warehouseList.forEach(warehouse ->
            log.info("WAREHOUSE ROW: " + warehouse)
        );

        assertNotNull(warehouseList);
        assertFalse(warehouseList.isEmpty());
    }

    // 현재고 등록과 수정 확인
    @Test
    public void testInsertAndUpdate() {
        ItemVO item = createItem();
        itemMapper.insertSelectKey(item);

        WarehouseLookupVO warehouse =
            stockMapper.getWarehouseList().get(0);

        StockVO stock = new StockVO();
        stock.setWarehouseId(warehouse.getWarehouseId());
        stock.setItemId(item.getItemId());
        stock.setQuantity(new BigDecimal("10"));

        log.info("========== STOCK INSERT TEST ==========");
        log.info(
            "WAREHOUSE ID: "
                + stock.getWarehouseId()
        );
        log.info("ITEM ID: " + stock.getItemId());
        log.info(
            "INSERT QUANTITY: "
                + stock.getQuantity()
        );

        stockMapper.insert(stock);

        StockVO savedStock = stockMapper.read(
            stock.getWarehouseId(),
            stock.getItemId()
        );

        log.info("SAVED STOCK: " + savedStock);

        assertNotNull(savedStock);
        assertEquals(
            0,
            new BigDecimal("10").compareTo(
                savedStock.getQuantity()
            )
        );

        stock.setQuantity(new BigDecimal("15"));

        int updateCount = stockMapper.update(stock);

        StockVO changedStock = stockMapper.read(
            stock.getWarehouseId(),
            stock.getItemId()
        );

        log.info("========== STOCK UPDATE TEST ==========");
        log.info("UPDATE COUNT: " + updateCount);
        log.info("UPDATED STOCK: " + changedStock);

        assertEquals(1, updateCount);
        assertEquals(
            0,
            new BigDecimal("15").compareTo(
                changedStock.getQuantity()
            )
        );
    }

    // 재고 변동 이력 등록 확인
    @Test
    public void testInsertMovement() {
        ItemVO item = createItem();
        itemMapper.insertSelectKey(item);

        WarehouseLookupVO warehouse =
            stockMapper.getWarehouseList().get(0);

        StockVO stock = new StockVO();
        stock.setWarehouseId(warehouse.getWarehouseId());
        stock.setItemId(item.getItemId());
        stock.setQuantity(BigDecimal.ZERO);

        stockMapper.insert(stock);

        StockMovementVO movement = new StockMovementVO();
        movement.setWarehouseId(warehouse.getWarehouseId());
        movement.setItemId(item.getItemId());
        movement.setQuantityDelta(new BigDecimal("5"));

        log.info("========== STOCK MOVEMENT INSERT TEST ==========");
        log.info(
            "WAREHOUSE ID: "
                + movement.getWarehouseId()
        );
        log.info("ITEM ID: " + movement.getItemId());
        log.info(
            "QUANTITY DELTA: "
                + movement.getQuantityDelta()
        );

        stockMapper.insertMovement(movement);

        log.info("STOCK MOVEMENT INSERT COMPLETED");
    }

    // 테스트 품목 생성
    private ItemVO createItem() {
        String token = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12);

        ItemVO item = new ItemVO();
        item.setItemCode("STOCK" + token);
        item.setItemName("재고 테스트 품목 " + token);
        item.setSpecification("100mm");
        item.setUnit("EA");
        item.setItemType("상품");
        item.setInboundPrice(new BigDecimal("10000"));
        item.setOutboundPrice(new BigDecimal("15000"));
        item.setNote("재고 Mapper 테스트 품목");

        return item;
    }
}
