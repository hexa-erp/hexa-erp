package hexa.erp.stock.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
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
import hexa.erp.stock.domain.StockVO;
import hexa.erp.stock.mapper.StockMapper;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    "file:src/main/webapp/WEB-INF/spring/root-context.xml"
)
@Transactional
@Rollback
@Log4j
public class StockServiceTests {

    @Autowired
    private StockService service;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private ItemMapper itemMapper;

    // Service 객체 주입 확인
    @Test
    public void testServiceExist() {
        log.info("========== SERVICE INJECTION TEST ==========");
        log.info("STOCK SERVICE: " + service);

        assertNotNull(service);
    }

    // 신규 현재고 생성과 조정 확인
    @Test
    public void testAdjust() {
        ItemVO item = createItem();
        itemMapper.insertSelectKey(item);

        WarehouseLookupVO warehouse =
            service.getWarehouseList().get(0);

        StockVO stock = new StockVO();
        stock.setWarehouseId(warehouse.getWarehouseId());
        stock.setItemId(item.getItemId());
        stock.setQuantity(
            new BigDecimal("10.1236")
        );

        log.info("========== STOCK ADJUST TEST ==========");
        log.info(
            "WAREHOUSE ID: "
                + stock.getWarehouseId()
        );
        log.info("ITEM ID: " + stock.getItemId());
        log.info(
            "INPUT QUANTITY: "
                + stock.getQuantity()
        );

        service.adjust(stock);

        StockVO savedStock = stockMapper.read(
            stock.getWarehouseId(),
            stock.getItemId()
        );

        log.info("SAVED STOCK: " + savedStock);

        assertNotNull(savedStock);
        assertEquals(
            0,
            new BigDecimal("10.124").compareTo(
                savedStock.getQuantity()
            )
        );

        stock.setQuantity(
            new BigDecimal("15.124")
        );

        service.adjust(stock);

        StockVO changedStock = stockMapper.read(
            stock.getWarehouseId(),
            stock.getItemId()
        );

        log.info("UPDATED STOCK: " + changedStock);

        assertEquals(
            0,
            new BigDecimal("15.124").compareTo(
                changedStock.getQuantity()
            )
        );
    }

    // 테스트 품목 생성
    private ItemVO createItem() {
        String token = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12);

        ItemVO item = new ItemVO();
        item.setItemCode("STOCKSERVICE" + token);
        item.setItemName(
            "재고 서비스 테스트 품목 " + token
        );
        item.setSpecification("100mm");
        item.setUnit("EA");
        item.setItemType("상품");
        item.setInboundPrice(
            new BigDecimal("10000")
        );
        item.setOutboundPrice(
            new BigDecimal("15000")
        );

        return item;
    }
}
