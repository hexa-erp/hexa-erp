package hexa.erp.sale.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
import hexa.erp.sale.domain.SaleCriteria;
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
public class SaleListServiceTests {

	@Autowired
	private SaleService service;

	@Autowired
	private ItemMapper itemMapper;

	@Autowired
	private StockMapper stockMapper;

	// 売上一覧と検索を確認
	@Test
	public void testGetList() {
		String token = createToken();

		SaleVO sale = saveSale(token);

		SaleCriteria criteria = new SaleCriteria();

		criteria.setKeyword(token);

		int totalCount = service.getTotal(criteria);

		List<SaleVO> saleList = service.getList(criteria);

		log.info("========== SALE SERVICE LIST TEST ==========");
		log.info("SEARCH KEYWORD: " + token);
		log.info("TOTAL COUNT: " + totalCount);

		saleList.forEach(row -> log.info("SALE ROW: " + row));

		assertEquals(1, totalCount);
		assertFalse(saleList.isEmpty());

		assertEquals(sale.getSaleId(), saleList.get(0).getSaleId());
	}

	// 進行状況の変更を確認
	@Test
	public void testChangeStatus() {
		SaleVO sale = saveSale(createToken());

		int updateCount = service.changeStatus(Collections.singletonList(sale.getSaleId()), "UNCONFIRMED");

		SaleVO changedSale = service.get(sale.getSaleId());

		log.info("========== SALE SERVICE STATUS TEST ==========");
		log.info("UPDATE COUNT: " + updateCount);
		log.info("CHANGED SALE: " + changedSale);

		assertEquals(1, updateCount);
		assertNotNull(changedSale);

		assertEquals("UNCONFIRMED", changedSale.getProgressStatus());
	}

	// 売上削除と在庫復元を確認
	@Test
	public void testRemove() {
		String token = createToken();

		SaleVO sale = saveSale(token);

		SaleLineVO line = sale.getLines().get(0);

		StockVO beforeStock = stockMapper.read(sale.getWarehouseId(), line.getItemId());

		int removeCount = service.remove(Collections.singletonList(sale.getSaleId()));

		SaleVO removedSale = service.get(sale.getSaleId());

		StockVO afterStock = stockMapper.read(sale.getWarehouseId(), line.getItemId());

		log.info("========== SALE SERVICE REMOVE TEST ==========");
		log.info("REMOVE COUNT: " + removeCount);
		log.info("BEFORE STOCK: " + beforeStock.getQuantity());
		log.info("AFTER STOCK: " + afterStock.getQuantity());
		log.info("REMOVED SALE: " + removedSale);

		assertEquals(1, removeCount);
		assertNull(removedSale);

		assertEquals(0, new BigDecimal("-3.000").compareTo(beforeStock.getQuantity()));

		assertEquals(0, BigDecimal.ZERO.setScale(3).compareTo(afterStock.getQuantity()));
	}

	// 同じ売上を再度削除しても在庫が二重に復元されないことを確認
	@Test
	public void testRemoveTwice() {
		SaleVO sale = saveSale(createToken());

		Long warehouseId = sale.getWarehouseId();
		Long itemId = sale.getLines().get(0).getItemId();

		List<Long> selectedIds = Collections.singletonList(sale.getSaleId());

		// 売上保存後の在庫は-3
		StockVO beforeStock = stockMapper.read(warehouseId, itemId);

		assertNotNull(beforeStock);
		assertEquals(0, new BigDecimal("-3").compareTo(beforeStock.getQuantity()));

		// 1回目の削除で在庫が0へ復元
		int firstRemoveCount = service.remove(selectedIds);
		StockVO firstStock = stockMapper.read(warehouseId, itemId);

		assertEquals(1, firstRemoveCount);
		assertNull(service.get(sale.getSaleId()));
		assertNotNull(firstStock);
		assertEquals(0, BigDecimal.ZERO.compareTo(firstStock.getQuantity()));

		// 2回目の削除では追加の削除や在庫復元は発生しない
		int secondRemoveCount = service.remove(selectedIds);
		StockVO secondStock = stockMapper.read(warehouseId, itemId);

		assertEquals(0, secondRemoveCount);
		assertNotNull(secondStock);
		assertEquals(0, BigDecimal.ZERO.compareTo(secondStock.getQuantity()));
	}

	// テスト売上を保存
	private SaleVO saveSale(String token) {
		ItemVO item = createItem(token);

		itemMapper.insertSelectKey(item);

		WarehouseLookupVO warehouse = stockMapper.getWarehouseList().get(0);

		SaleVO sale = new SaleVO();

		sale.setBusinessDate(LocalDate.now().toString());

		sale.setPartnerName("Service一覧テスト取引先 " + token);

		sale.setWarehouseId(warehouse.getWarehouseId());

		SaleLineVO line = new SaleLineVO();

		line.setItemId(item.getItemId());

		line.setQuantity(new BigDecimal("3"));

		line.setUnitPrice(new BigDecimal("10000"));

		sale.getLines().add(line);

		Long saleId = service.save(sale);

		assertNotNull(saleId);

		return service.get(saleId);
	}

	// テスト品目を生成
	private ItemVO createItem(String token) {

		ItemVO item = new ItemVO();

		item.setItemCode("SLISTSVC" + token);

		item.setItemName("Service一覧テスト品目 " + token);

		item.setSpecification("100mm");
		item.setUnit("EA");
		item.setItemType("商品");

		item.setInboundPrice(new BigDecimal("7000"));

		item.setOutboundPrice(new BigDecimal("10000"));

		item.setNote("売上一覧Serviceテスト");

		return item;
	}

	// テスト識別値を生成
	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
	}
}
