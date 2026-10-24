package hexa.erp.sale.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import hexa.erp.item.domain.ItemVO;
import hexa.erp.item.mapper.ItemMapper;
import hexa.erp.sale.domain.SaleLineVO;
import hexa.erp.sale.domain.SalePriceChangeVO;
import hexa.erp.sale.domain.SaleVO;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.stock.mapper.StockMapper;
import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.mapper.WarehouseMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
public class SalePriceServiceTests {

	@Autowired
	private SalePriceService service;

	@Autowired
	private SaleService saleService;

	@Autowired
	private ItemMapper itemMapper;

	@Autowired
	private WarehouseMapper warehouseMapper;

	@Autowired
	private StockMapper stockMapper;

	@Autowired
	private PlatformTransactionManager transactionManager;

	// 선택 행의 단가·금액 변경과 미선택 행·수량·재고 유지 확인
	@Test
	public void testChangeSelectedPricesWithoutChangingStock() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		SaleVO sale = createSale(item, warehouse);
		Long saleId = saleService.save(sale);

		assertNotNull(saleId);

		SaleVO saved = saleService.get(saleId);

		assertNotNull(saved);
		assertEquals(3, saved.getLines().size());
		assertDecimal("9900", saved.getTotalAmount());
		assertStockQuantity(warehouse, item, "-9");

		Long firstLineId = saved.getLines().get(0).getSaleLineId();
		Long secondLineId = saved.getLines().get(1).getSaleLineId();
		Long thirdLineId = saved.getLines().get(2).getSaleLineId();

		SalePriceChangeVO firstChange = createChange(saleId, firstLineId, true, "2000.555");
		SalePriceChangeVO secondChange = createChange(saleId, secondLineId, true, "2000.555");

		// 미선택 행은 다른 단가가 전달되어도 변경하지 않아야 함
		SalePriceChangeVO unselectedChange = createChange(saleId, thirdLineId, false, "9999");

		int changeCount = service.savePrices(Arrays.asList(firstChange, secondChange, unselectedChange));

		SaleVO changed = saleService.get(saleId);

		assertEquals(2, changeCount);
		assertNotNull(changed);
		assertEquals(3, changed.getLines().size());

		assertEquals(firstLineId, changed.getLines().get(0).getSaleLineId());
		assertEquals(secondLineId, changed.getLines().get(1).getSaleLineId());
		assertEquals(thirdLineId, changed.getLines().get(2).getSaleLineId());

		assertLineAmounts(changed.getLines().get(0), "2", "2000.56", "4001", "400", "4401");
		assertLineAmounts(changed.getLines().get(1), "3", "2000.56", "6002", "600", "6602");
		assertLineAmounts(changed.getLines().get(2), "4", "1000", "4000", "400", "4400");

		assertDecimal("15403", changed.getTotalAmount());
		assertStockQuantity(warehouse, item, "-9");
	}

	// 일괄 변경 중 실패하면 앞선 행의 변경까지 롤백되는지 확인
	@Test
	public void testRollbackAllPricesWhenOneChangeFails() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		SaleVO sale = createSale(item, warehouse);
		Long saleId = saleService.save(sale);

		SaleVO saved = saleService.get(saleId);

		assertNotNull(saved);
		assertEquals(3, saved.getLines().size());
		assertDecimal("9900", saved.getTotalAmount());
		assertStockQuantity(warehouse, item, "-9");

		Long firstLineId = saved.getLines().get(0).getSaleLineId();
		Long secondLineId = saved.getLines().get(1).getSaleLineId();

		// 첫 번째 행은 정상 변경 요청
		SalePriceChangeVO firstChange = createChange(saleId, firstLineId, true, "2000");

		// 두 번째 행은 단가가 없어 실패하도록 구성
		SalePriceChangeVO invalidChange = createChange(saleId, secondLineId, true, "3000");
		invalidChange.setUnitPrice(null);

		// 테스트용 판매 데이터는 유지하고 단가 변경 부분의 롤백을 검증
		TransactionTemplate nestedTransaction = new TransactionTemplate(transactionManager);

		nestedTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);

		assertThrows(UnexpectedRollbackException.class, () -> {
			nestedTransaction.execute(status -> {
				assertThrows(IllegalArgumentException.class,
						() -> service.savePrices(Arrays.asList(firstChange, invalidChange)));
				return null;
			});
		});

		// 실패 후 실제 저장값을 다시 조회
		SaleVO unchanged = saleService.get(saleId);

		assertNotNull(unchanged);
		assertEquals(3, unchanged.getLines().size());

		assertLineAmounts(unchanged.getLines().get(0), "2", "1000", "2000", "200", "2200");
		assertLineAmounts(unchanged.getLines().get(1), "3", "1000", "3000", "300", "3300");
		assertLineAmounts(unchanged.getLines().get(2), "4", "1000", "4000", "400", "4400");

		assertDecimal("9900", unchanged.getTotalAmount());
		assertStockQuantity(warehouse, item, "-9");
	}

	// 같은 품목을 서로 다른 상세행 3개로 구성
	private SaleVO createSale(ItemVO item, WarehouseVO warehouse) {
		SaleVO sale = new SaleVO();
		sale.setBusinessDate(LocalDate.now().toString());
		sale.setWarehouseId(warehouse.getWarehouseId());
		sale.setNote("단가 일괄 변경 테스트");

		sale.getLines().add(createLine(item, "2"));
		sale.getLines().add(createLine(item, "3"));
		sale.getLines().add(createLine(item, "4"));

		return sale;
	}

	private SaleLineVO createLine(ItemVO item, String quantity) {
		SaleLineVO line = new SaleLineVO();
		line.setItemId(item.getItemId());
		line.setItemName(item.getItemName());
		line.setSpecification(item.getSpecification());
		line.setUnit(item.getUnit());
		line.setQuantity(new BigDecimal(quantity));
		line.setUnitPrice(new BigDecimal("1000"));

		return line;
	}

	// 단가 변경 요청 생성
	private SalePriceChangeVO createChange(Long saleId, Long saleLineId, boolean selected, String unitPrice) {

		SalePriceChangeVO change = new SalePriceChangeVO();
		change.setSaleId(saleId);
		change.setSaleLineId(saleLineId);
		change.setSelected(selected);
		change.setUnitPrice(new BigDecimal(unitPrice));

		return change;
	}

	// 수량·단가·금액 확인
	private void assertLineAmounts(SaleLineVO line, String quantity, String unitPrice, String supplyAmount,
			String vatAmount, String totalAmount) {

		assertDecimal(quantity, line.getQuantity());
		assertDecimal(unitPrice, line.getUnitPrice());
		assertDecimal(supplyAmount, line.getSupplyAmount());
		assertDecimal(vatAmount, line.getVatAmount());
		assertDecimal(totalAmount, line.getTotalAmount());
	}

	private void assertDecimal(String expected, BigDecimal actual) {
		assertNotNull(actual);
		assertEquals(0, new BigDecimal(expected).compareTo(actual));
	}

	private void assertStockQuantity(WarehouseVO warehouse, ItemVO item, String expectedQuantity) {

		StockVO stock = stockMapper.read(warehouse.getWarehouseId(), item.getItemId());

		assertNotNull(stock);
		assertDecimal(expectedQuantity, stock.getQuantity());
	}

	private ItemVO createItem() {
		String token = createToken();

		ItemVO item = new ItemVO();
		item.setItemCode("SPTEST" + token);
		item.setItemName("단가 변경 테스트 품목 " + token);
		item.setSpecification("100mm");
		item.setUnit("EA");
		item.setItemType("상품");
		item.setInboundPrice(BigDecimal.ZERO);
		item.setOutboundPrice(new BigDecimal("1000"));

		return item;
	}

	private WarehouseVO createWarehouse() {
		String token = createToken();

		WarehouseVO warehouse = new WarehouseVO();
		warehouse.setWarehouseCode("SPWTEST" + token);
		warehouse.setWarehouseName("단가 변경 테스트 창고 " + token);
		warehouse.setWarehouseType("창고");

		return warehouse;
	}

	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}