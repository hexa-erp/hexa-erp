package hexa.erp.sale.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
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
import hexa.erp.sale.domain.SaleLineVO;
import hexa.erp.sale.domain.SaleVO;
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
public class SaleMapperTests {

    @Autowired
    private SaleMapper saleMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private StockMapper stockMapper;

    // Mapper 객체 주입 확인
    @Test
    public void testMapperExist() {
        log.info("========== MAPPER INJECTION TEST ==========");
        log.info("SALE MAPPER: " + saleMapper);
        log.info("ITEM MAPPER: " + itemMapper);
        log.info("STOCK MAPPER: " + stockMapper);

        assertNotNull(saleMapper);
        assertNotNull(itemMapper);
        assertNotNull(stockMapper);
    }

    // 판매 기본정보와 상세행 등록 확인
    @Test
    public void testInsertAndRead() {
        SaleVO sale = createSale();

        log.info("========== SALE INSERT TEST ==========");
        log.info("INSERT BEFORE: " + sale);

        saleMapper.insertSelectKey(sale);

        ItemVO item = createItem();
        itemMapper.insertSelectKey(item);

        SaleLineVO line =
            createLine(sale.getSaleId(), item);

        saleMapper.insertLineSelectKey(line);

        SaleVO savedSale =
            saleMapper.read(sale.getSaleId());

        List<SaleLineVO> savedLines =
            saleMapper.getLines(sale.getSaleId());

        log.info("GENERATED SALE ID: " + sale.getSaleId());
        log.info("GENERATED SALE NO: " + savedSale.getSaleNo());
        log.info("GENERATED LINE ID: " + line.getSaleLineId());
        log.info("SAVED SALE: " + savedSale);
        savedLines.forEach(savedLine ->
            log.info("SAVED LINE: " + savedLine)
        );

        assertNotNull(sale.getSaleId());
        assertNotNull(line.getSaleLineId());
        assertNotNull(savedSale);
        assertEquals(1, savedLines.size());
        assertEquals(
            sale.getBusinessDate(),
            savedSale.getBusinessDate()
        );
        assertEquals(
            item.getItemId(),
            savedLines.get(0).getItemId()
        );
    }

    // 판매 기본정보와 상세행 수정 확인
    @Test
    public void testUpdate() {
        SaleVO sale = createSale();
        saleMapper.insertSelectKey(sale);

        ItemVO item = createItem();
        itemMapper.insertSelectKey(item);

        SaleLineVO line =
            createLine(sale.getSaleId(), item);

        saleMapper.insertLineSelectKey(line);

        sale.setNote("수정된 판매 비고");
        line.setQuantity(new BigDecimal("5.000"));
        line.setSupplyAmount(new BigDecimal("50000"));
        line.setVatAmount(new BigDecimal("5000"));
        line.setTotalAmount(new BigDecimal("55000"));

        log.info("========== SALE UPDATE TEST ==========");
        log.info("SALE ID: " + sale.getSaleId());
        log.info("LINE ID: " + line.getSaleLineId());
        log.info("CHANGED SALE: " + sale);
        log.info("CHANGED LINE: " + line);

        int saleUpdateCount = saleMapper.update(sale);
        int lineUpdateCount = saleMapper.updateLine(line);

        SaleVO changedSale =
            saleMapper.read(sale.getSaleId());

        SaleLineVO changedLine =
            saleMapper.getLines(sale.getSaleId()).get(0);

        log.info("SALE UPDATE COUNT: " + saleUpdateCount);
        log.info("LINE UPDATE COUNT: " + lineUpdateCount);
        log.info("UPDATED SALE: " + changedSale);
        log.info("UPDATED LINE: " + changedLine);

        assertEquals(1, saleUpdateCount);
        assertEquals(1, lineUpdateCount);
        assertEquals(
            "수정된 판매 비고",
            changedSale.getNote()
        );
        assertEquals(
            0,
            new BigDecimal("5.000").compareTo(
                changedLine.getQuantity()
            )
        );
    }

    // 선택한 판매 상세행 삭제 처리 확인
    @Test
    public void testRemoveSelectedLines() {
        SaleVO sale = createSale();
        saleMapper.insertSelectKey(sale);

        ItemVO item = createItem();
        itemMapper.insertSelectKey(item);

        SaleLineVO line =
            createLine(sale.getSaleId(), item);

        saleMapper.insertLineSelectKey(line);

        log.info("========== SALE LINE REMOVE TEST ==========");
        log.info("SALE ID: " + sale.getSaleId());
        log.info("REMOVE LINE ID: " + line.getSaleLineId());

        int removeCount =
            saleMapper.removeSelectedLines(
                sale.getSaleId(),
                Collections.singletonList(
                    line.getSaleLineId()
                )
            );

        List<SaleLineVO> savedLines =
            saleMapper.getLines(sale.getSaleId());

        log.info("REMOVE COUNT: " + removeCount);
        log.info(
            "ACTIVE LINE COUNT: "
                + savedLines.size()
        );

        assertEquals(1, removeCount);
        assertTrue(savedLines.isEmpty());
    }

    // 테스트 판매 생성
    private SaleVO createSale() {
        WarehouseLookupVO warehouse =
            stockMapper.getWarehouseList().get(0);

        SaleVO sale = new SaleVO();
        sale.setBusinessDate(
            LocalDate.now().toString()
        );
        sale.setWarehouseId(
            warehouse.getWarehouseId()
        );
        sale.setWarehouseName(
            warehouse.getWarehouseName()
        );
        sale.setNote("Mapper 테스트 판매");

        return sale;
    }

    // 테스트 판매 상세행 생성
    private SaleLineVO createLine(
            Long saleId,
            ItemVO item) {

        SaleLineVO line = new SaleLineVO();
        line.setSaleId(saleId);
        line.setItemId(item.getItemId());
        line.setItemName(item.getItemName());
        line.setSpecification(
            item.getSpecification()
        );
        line.setUnit(item.getUnit());
        line.setQuantity(
            new BigDecimal("3.000")
        );
        line.setUnitPrice(
            new BigDecimal("10000.00")
        );
        line.setSupplyAmount(
            new BigDecimal("30000")
        );
        line.setVatAmount(
            new BigDecimal("3000")
        );
        line.setTotalAmount(
            new BigDecimal("33000")
        );
        line.setNote("판매 상세 테스트");

        return line;
    }

    // 테스트 품목 생성
    private ItemVO createItem() {
        String token = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 12);

        ItemVO item = new ItemVO();
        item.setItemCode("SALE" + token);
        item.setItemName("판매 테스트 품목 " + token);
        item.setSpecification("100mm");
        item.setUnit("EA");
        item.setItemType("상품");
        item.setInboundPrice(
            new BigDecimal("7000")
        );
        item.setOutboundPrice(
            new BigDecimal("10000")
        );
        item.setNote("판매 Mapper 테스트");

        return item;
    }
}
