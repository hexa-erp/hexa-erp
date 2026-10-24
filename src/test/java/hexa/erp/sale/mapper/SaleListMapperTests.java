package hexa.erp.sale.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import hexa.erp.sale.domain.SaleCriteria;
import hexa.erp.sale.domain.SaleLineVO;
import hexa.erp.sale.domain.SaleVO;
import hexa.erp.stock.mapper.StockMapper;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
@Log4j
public class SaleListMapperTests {

	@Autowired
	private SaleMapper saleMapper;

	@Autowired
	private ItemMapper itemMapper;

	@Autowired
	private StockMapper stockMapper;

	// 売上一覧と検索を確認
	@Test
	public void testGetListWithPaging() {
		String token = createToken();

		SaleVO sale = saveSale(token);

		SaleCriteria criteria = new SaleCriteria();

		criteria.setKeyword(token);
		criteria.setProgressStatus("CONFIRMED");

		int totalCount = saleMapper.getTotalCount(criteria);

		List<SaleVO> saleList = saleMapper.getListWithPaging(criteria);

		log.info("========== SALE LIST TEST ==========");
		log.info("SEARCH KEYWORD: " + criteria.getKeyword());
		log.info("TOTAL COUNT: " + totalCount);
		log.info("LIST COUNT: " + saleList.size());

		saleList.forEach(row -> {
			log.info("SALE ROW: " + row);
			log.info("ITEM SUMMARY: " + row.getItemSummary());
			log.info("TOTAL AMOUNT: " + row.getTotalAmount());
		});

		assertEquals(1, totalCount);
		assertFalse(saleList.isEmpty());
		assertEquals(sale.getSaleId(), saleList.get(0).getSaleId());
		assertTrue(saleList.get(0).getItemSummary().contains(token));
		assertEquals(0, new BigDecimal("33000").compareTo(saleList.get(0).getTotalAmount()));
	}

	// 進行状況の変更を確認
	@Test
	public void testChangeStatus() {
		SaleVO sale = saveSale(createToken());

		int updateCount = saleMapper.changeStatus(Collections.singletonList(sale.getSaleId()), "UNCONFIRMED");

		SaleVO changedSale = saleMapper.read(sale.getSaleId());

		log.info("========== STATUS CHANGE TEST ==========");
		log.info("SALE ID: " + sale.getSaleId());
		log.info("UPDATE COUNT: " + updateCount);
		log.info("CHANGED STATUS: " + changedSale.getProgressStatus());

		assertEquals(1, updateCount);
		assertNotNull(changedSale);
		assertEquals("UNCONFIRMED", changedSale.getProgressStatus());
	}

	// 売上の論理削除を確認
	@Test
	public void testRemove() {
		SaleVO sale = saveSale(createToken());

		List<Long> ids = Collections.singletonList(sale.getSaleId());

		int lineRemoveCount = saleMapper.removeLines(ids);

		int saleRemoveCount = saleMapper.remove(ids);

		SaleVO removedSale = saleMapper.read(sale.getSaleId());

		List<SaleLineVO> removedLines = saleMapper.getLines(sale.getSaleId());

		log.info("========== SALE REMOVE TEST ==========");
		log.info("SALE ID: " + sale.getSaleId());
		log.info("LINE REMOVE COUNT: " + lineRemoveCount);
		log.info("SALE REMOVE COUNT: " + saleRemoveCount);
		log.info("REMOVED SALE: " + removedSale);
		log.info("ACTIVE LINE COUNT: " + removedLines.size());

		assertEquals(1, lineRemoveCount);
		assertEquals(1, saleRemoveCount);
		assertNull(removedSale);
		assertTrue(removedLines.isEmpty());
	}

	// テスト売上を保存
	private SaleVO saveSale(String token) {
		WarehouseLookupVO warehouse = stockMapper.getWarehouseList().get(0);

		ItemVO item = createItem(token);
		itemMapper.insertSelectKey(item);

		SaleVO sale = new SaleVO();

		sale.setBusinessDate(LocalDate.now().toString());
		sale.setPartnerName("一覧テスト取引先 " + token);
		sale.setWarehouseId(warehouse.getWarehouseId());
		sale.setWarehouseName(warehouse.getWarehouseName());
		sale.setNote("売上一覧Mapperテスト");

		saleMapper.insertSelectKey(sale);

		SaleLineVO line = new SaleLineVO();

		line.setSaleId(sale.getSaleId());
		line.setItemId(item.getItemId());
		line.setItemName(item.getItemName());
		line.setSpecification(item.getSpecification());
		line.setUnit(item.getUnit());
		line.setQuantity(new BigDecimal("3.000"));
		line.setUnitPrice(new BigDecimal("10000.00"));
		line.setSupplyAmount(new BigDecimal("30000"));
		line.setVatAmount(new BigDecimal("3000"));
		line.setTotalAmount(new BigDecimal("33000"));
		line.setNote("売上一覧明細テスト");

		saleMapper.insertLineSelectKey(line);

		sale.getLines().add(line);

		return sale;
	}

	// テスト品目を生成
	private ItemVO createItem(String token) {

		ItemVO item = new ItemVO();

		item.setItemCode("SALELIST" + token);
		item.setItemName("一覧テスト品目 " + token);
		item.setSpecification("100mm");
		item.setUnit("EA");
		item.setItemType("商品");
		item.setInboundPrice(new BigDecimal("7000"));
		item.setOutboundPrice(new BigDecimal("10000"));
		item.setNote("売上一覧Mapperテスト品目");

		return item;
	}

	// テスト識別値を生成
	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
	}
}
