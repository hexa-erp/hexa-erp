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

	// 판매 목록과 검색 확인
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

	// 진행상태 변경 확인
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

	// 판매 삭제와 재고 복구 확인
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

	// 같은 판매를 다시 삭제해도 재고가 중복 복원되지 않는지 확인
	@Test
	public void testRemoveTwice() {
		SaleVO sale = saveSale(createToken());

		Long warehouseId = sale.getWarehouseId();
		Long itemId = sale.getLines().get(0).getItemId();

		List<Long> selectedIds = Collections.singletonList(sale.getSaleId());

		// 판매 저장 후 재고는 -3
		StockVO beforeStock = stockMapper.read(warehouseId, itemId);

		assertNotNull(beforeStock);
		assertEquals(0, new BigDecimal("-3").compareTo(beforeStock.getQuantity()));

		// 첫 번째 삭제: 재고가 0으로 복원
		int firstRemoveCount = service.remove(selectedIds);
		StockVO firstStock = stockMapper.read(warehouseId, itemId);

		assertEquals(1, firstRemoveCount);
		assertNull(service.get(sale.getSaleId()));
		assertNotNull(firstStock);
		assertEquals(0, BigDecimal.ZERO.compareTo(firstStock.getQuantity()));

		// 두 번째 삭제: 추가 삭제나 재고 복원이 발생하지 않음
		int secondRemoveCount = service.remove(selectedIds);
		StockVO secondStock = stockMapper.read(warehouseId, itemId);

		assertEquals(0, secondRemoveCount);
		assertNotNull(secondStock);
		assertEquals(0, BigDecimal.ZERO.compareTo(secondStock.getQuantity()));
	}

	// 테스트 판매 저장
	private SaleVO saveSale(String token) {
		ItemVO item = createItem(token);

		itemMapper.insertSelectKey(item);

		WarehouseLookupVO warehouse = stockMapper.getWarehouseList().get(0);

		SaleVO sale = new SaleVO();

		sale.setBusinessDate(LocalDate.now().toString());

		sale.setPartnerName("서비스 조회 거래처 " + token);

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

	// 테스트 품목 생성
	private ItemVO createItem(String token) {

		ItemVO item = new ItemVO();

		item.setItemCode("SLISTSVC" + token);

		item.setItemName("서비스 조회 품목 " + token);

		item.setSpecification("100mm");
		item.setUnit("EA");
		item.setItemType("상품");

		item.setInboundPrice(new BigDecimal("7000"));

		item.setOutboundPrice(new BigDecimal("10000"));

		item.setNote("판매 조회 Service 테스트");

		return item;
	}

	// 테스트 구분값 생성
	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
	}
}
