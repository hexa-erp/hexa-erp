package hexa.erp.sale.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import hexa.erp.stock.domain.StockVO;
import hexa.erp.stock.mapper.StockMapper;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
@Log4j
public class SaleServiceTests {

	@Autowired
	private SaleService service;

	@Autowired
	private ItemMapper itemMapper;

	@Autowired
	private StockMapper stockMapper;

	// Serviceオブジェクトの注入を確認
	@Test
	public void testServiceExist() {
		log.info("========== SERVICE INJECTION TEST ==========");
		log.info("SALE SERVICE: " + service);

		assertNotNull(service);
	}

	// 売上保存と在庫減少を確認
	@Test
	public void testSave() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseLookupVO warehouse = stockMapper.getWarehouseList().get(0);

		SaleVO sale = createSale(item, warehouse, "3");

		log.info("========== SALE SAVE TEST ==========");
		log.info("WAREHOUSE ID: " + sale.getWarehouseId());
		log.info("ITEM ID: " + item.getItemId());
		log.info("SALE QUANTITY: " + sale.getLines().get(0).getQuantity());

		Long saleId = service.save(sale);

		assertNotNull(saleId);

		SaleVO savedSale = service.get(saleId);

		StockVO stock = stockMapper.read(warehouse.getWarehouseId(), item.getItemId());

		assertNotNull(savedSale);
		assertNotNull(stock);

		SaleLineVO savedLine = savedSale.getLines().get(0);

		log.info("SAVED SALE ID: " + saleId);
		log.info("SAVED SALE: " + savedSale);
		log.info("SAVED LINE: " + savedLine);
		log.info("STOCK QUANTITY: " + stock.getQuantity());

		assertEquals(1, savedSale.getLines().size());

		assertEquals(0, new BigDecimal("3.000").compareTo(savedLine.getQuantity()));

		assertEquals(0, new BigDecimal("10000.00").compareTo(savedLine.getUnitPrice()));

		assertEquals(0, new BigDecimal("30000").compareTo(savedLine.getSupplyAmount()));

		assertEquals(0, new BigDecimal("3000").compareTo(savedLine.getVatAmount()));

		assertEquals(0, new BigDecimal("33000").compareTo(savedLine.getTotalAmount()));

		assertEquals(0, new BigDecimal("-3.000").compareTo(stock.getQuantity()));
	}

	// 売上数量の更新と在庫再計算を確認
	@Test
	public void testUpdate() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseLookupVO warehouse = stockMapper.getWarehouseList().get(0);

		SaleVO sale = createSale(item, warehouse, "3");

		Long saleId = service.save(sale);

		assertNotNull(saleId);
		assertNotNull(sale.getLines());
		assertEquals(1, sale.getLines().size());

		Long saleLineId = sale.getLines().get(0).getSaleLineId();

		assertNotNull(saleLineId);

		SaleVO updateSale = new SaleVO();
		updateSale.setSaleId(saleId);
		updateSale.setBusinessDate(LocalDate.now().toString());
		updateSale.setWarehouseId(warehouse.getWarehouseId());
		updateSale.setNote("売上Service更新テスト");

		SaleLineVO updateLine = new SaleLineVO();

		updateLine.setSaleLineId(saleLineId);
		updateLine.setItemId(item.getItemId());
		updateLine.setQuantity(new BigDecimal("5"));
		updateLine.setUnitPrice(new BigDecimal("10000"));
		updateLine.setNote("売上明細の更新テスト");

		updateSale.getLines().add(updateLine);

		log.info("========== SALE UPDATE TEST ==========");
		log.info("SALE ID: " + saleId);
		log.info("SALE LINE ID: " + saleLineId);
		log.info("BEFORE QUANTITY: 3");
		log.info("AFTER QUANTITY: 5");

		service.save(updateSale);

		SaleVO changedSale = service.get(saleId);

		StockVO stock = stockMapper.read(warehouse.getWarehouseId(), item.getItemId());

		assertNotNull(changedSale);
		assertNotNull(stock);
		assertNotNull(changedSale.getLines());

		assertEquals(1, changedSale.getLines().size());

		SaleLineVO changedLine = changedSale.getLines().get(0);

		log.info("CHANGED SALE: " + changedSale);
		log.info("CHANGED LINE: " + changedLine);
		log.info("CHANGED STOCK QUANTITY: " + stock.getQuantity());

		assertEquals(0, new BigDecimal("5.000").compareTo(changedLine.getQuantity()));

		assertEquals(0, new BigDecimal("50000").compareTo(changedLine.getSupplyAmount()));

		assertEquals(0, new BigDecimal("5000").compareTo(changedLine.getVatAmount()));

		assertEquals(0, new BigDecimal("55000").compareTo(changedLine.getTotalAmount()));

		assertEquals(0, new BigDecimal("-5.000").compareTo(stock.getQuantity()));
	}

	// テスト売上を生成
	private SaleVO createSale(ItemVO item, WarehouseLookupVO warehouse, String quantity) {

		SaleVO sale = new SaleVO();

		sale.setBusinessDate(LocalDate.now().toString());
		sale.setWarehouseId(warehouse.getWarehouseId());
		sale.setNote("売上Serviceテスト");

		SaleLineVO line = new SaleLineVO();

		line.setItemId(item.getItemId());
		line.setQuantity(new BigDecimal(quantity));
		line.setUnitPrice(new BigDecimal("10000"));
		line.setNote("売上明細テスト");

		sale.getLines().add(line);

		return sale;
	}

	// テスト品目を生成
	private ItemVO createItem() {
		String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		ItemVO item = new ItemVO();

		item.setItemCode("SALESERVICE" + token);
		item.setItemName("売上Serviceテスト品目 " + token);
		item.setSpecification("100mm");
		item.setUnit("EA");
		item.setItemType("商品");
		item.setInboundPrice(new BigDecimal("7000"));
		item.setOutboundPrice(new BigDecimal("10000"));
		item.setNote("売上Serviceテスト品目");

		return item;
	}
}
