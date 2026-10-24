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

	// 選択行の単価・金額変更と、未選択行・数量・在庫の保持を確認
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

		// 未選択行は異なる単価を渡しても変更しない
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

	// 一括変更中に失敗した場合、先行行の変更もロールバックされることを確認
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

		// 1行目は正常な変更リクエスト
		SalePriceChangeVO firstChange = createChange(saleId, firstLineId, true, "2000");

		// 2行目は単価を未設定にし、失敗させる
		SalePriceChangeVO invalidChange = createChange(saleId, secondLineId, true, "3000");
		invalidChange.setUnitPrice(null);

		// テスト用売上データを保持し、単価変更部分のロールバックを検証
		TransactionTemplate nestedTransaction = new TransactionTemplate(transactionManager);

		nestedTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);

		assertThrows(UnexpectedRollbackException.class, () -> {
			nestedTransaction.execute(status -> {
				assertThrows(IllegalArgumentException.class,
						() -> service.savePrices(Arrays.asList(firstChange, invalidChange)));
				return null;
			});
		});

		// 失敗後に実際の保存値を再取得
		SaleVO unchanged = saleService.get(saleId);

		assertNotNull(unchanged);
		assertEquals(3, unchanged.getLines().size());

		assertLineAmounts(unchanged.getLines().get(0), "2", "1000", "2000", "200", "2200");
		assertLineAmounts(unchanged.getLines().get(1), "3", "1000", "3000", "300", "3300");
		assertLineAmounts(unchanged.getLines().get(2), "4", "1000", "4000", "400", "4400");

		assertDecimal("9900", unchanged.getTotalAmount());
		assertStockQuantity(warehouse, item, "-9");
	}

	// 同じ品目を異なる3つの明細行に設定
	private SaleVO createSale(ItemVO item, WarehouseVO warehouse) {
		SaleVO sale = new SaleVO();
		sale.setBusinessDate(LocalDate.now().toString());
		sale.setWarehouseId(warehouse.getWarehouseId());
		sale.setNote("単価一括変更テスト");

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

	// 単価変更リクエストを生成
	private SalePriceChangeVO createChange(Long saleId, Long saleLineId, boolean selected, String unitPrice) {

		SalePriceChangeVO change = new SalePriceChangeVO();
		change.setSaleId(saleId);
		change.setSaleLineId(saleLineId);
		change.setSelected(selected);
		change.setUnitPrice(new BigDecimal(unitPrice));

		return change;
	}

	// 数量・単価・金額を確認
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
		item.setItemName("単価変更テスト品目 " + token);
		item.setSpecification("100mm");
		item.setUnit("EA");
		item.setItemType("商品");
		item.setInboundPrice(BigDecimal.ZERO);
		item.setOutboundPrice(new BigDecimal("1000"));

		return item;
	}

	private WarehouseVO createWarehouse() {
		String token = createToken();

		WarehouseVO warehouse = new WarehouseVO();
		warehouse.setWarehouseCode("SPWTEST" + token);
		warehouse.setWarehouseName("単価変更テスト倉庫 " + token);
		warehouse.setWarehouseType("倉庫");

		return warehouse;
	}

	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}