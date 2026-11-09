package hexa.erp.salesorder.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import hexa.erp.sale.service.SaleService;
import hexa.erp.salesorder.domain.SalesOrderLineVO;
import hexa.erp.salesorder.domain.SalesOrderReportCriteria;
import hexa.erp.salesorder.domain.SalesOrderReportRowVO;
import hexa.erp.salesorder.domain.SalesOrderReportVO;
import hexa.erp.salesorder.domain.SalesOrderVO;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.stock.mapper.StockMapper;
import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.mapper.WarehouseMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
public class SalesOrderReportServiceTests {

	@Autowired
	private SalesOrderReportService service;

	@Autowired
	private SalesOrderService salesOrderService;

	@Autowired
	private SaleService saleService;

	@Autowired
	private ItemMapper itemMapper;

	@Autowired
	private WarehouseMapper warehouseMapper;

	@Autowired
	private StockMapper stockMapper;

	// 未売上一覧の基準日、売上更新・削除、残数・在庫への反映を確認
	@Test
	public void testUnsoldCutoffDateAndSaleChanges() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 元受注10個を保存
		SalesOrderVO order = createOrder(item, warehouse);
		Long orderId = salesOrderService.save(order);
		Long orderLineId = order.getLines().get(0).getSalesOrderLineId();

		assertNotNull(orderId);
		assertNotNull(orderLineId);

		// 1月31日の売上3個
		SaleVO januarySale = createLinkedSale(item, warehouse, orderLineId, "2026-01-31", "3");

		Long januarySaleId = saleService.save(januarySale);

		assertNotNull(januarySaleId);
		assertEquals(orderLineId, januarySale.getLines().get(0).getSalesOrderLineId());

		// 2月1日の売上4個
		SaleVO februarySale = createLinkedSale(item, warehouse, orderLineId, "2026-02-01", "4");

		Long februarySaleId = saleService.save(februarySale);

		assertNotNull(februarySaleId);
		assertEquals(orderLineId, februarySale.getLines().get(0).getSalesOrderLineId());
		assertStockQuantity(warehouse, item, "-7");

		SalesOrderReportCriteria criteria = new SalesOrderReportCriteria();
		criteria.setItemIds(Collections.singletonList(item.getItemId().toString()));

		// 1月31日時点：売上3個だけを差し引く → 残数7
		criteria.setCutoffDate("2026-01-31");
		assertUnsoldReport(criteria, orderId, orderLineId, "7", "7000", "700");
		assertStockQuantity(warehouse, item, "-7");

		// 2月1日時点：売上3個と4個を差し引く → 残数3
		criteria.setCutoffDate("2026-02-01");
		assertUnsoldReport(criteria, orderId, orderLineId, "3", "3000", "300");
		assertStockQuantity(warehouse, item, "-7");

		// 既存の売上明細行IDを確認
		SaleVO existingSale = saleService.get(februarySaleId);

		assertNotNull(existingSale);
		assertEquals(1, existingSale.getLines().size());

		Long saleLineId = existingSale.getLines().get(0).getSaleLineId();

		// 更新リクエストは別のVOで作成：売上数量4 → 5
		SaleVO update = createLinkedSale(item, warehouse, orderLineId, "2026-02-01", "5");

		update.setSaleId(februarySaleId);
		update.getLines().get(0).setSaleLineId(saleLineId);

		saleService.save(update);

		SaleVO changedSale = saleService.get(februarySaleId);

		assertNotNull(changedSale);
		assertEquals(1, changedSale.getLines().size());
		assertEquals(saleLineId, changedSale.getLines().get(0).getSaleLineId());
		assertEquals(orderLineId, changedSale.getLines().get(0).getSalesOrderLineId());
		assertDecimal("5", changedSale.getLines().get(0).getQuantity());

		// 売上合計8個 → 残数2、在庫-8
		assertUnsoldReport(criteria, orderId, orderLineId, "2", "2000", "200");
		assertStockQuantity(warehouse, item, "-8");

		// 2月1日の売上を削除 → 1月31日の売上3個だけを保持
		assertEquals(1, saleService.remove(Collections.singletonList(februarySaleId)));
		assertNull(saleService.get(februarySaleId));

		assertUnsoldReport(criteria, orderId, orderLineId, "7", "7000", "700");
		assertStockQuantity(warehouse, item, "-3");
	}

	// 元受注10個を生成
	private SalesOrderVO createOrder(ItemVO item, WarehouseVO warehouse) {

		SalesOrderVO order = new SalesOrderVO();
		order.setBusinessDate("2026-01-01");
		order.setWarehouseId(warehouse.getWarehouseId());
		order.setNote("未売上一覧の元受注テスト");

		SalesOrderLineVO line = new SalesOrderLineVO();
		line.setItemId(item.getItemId());
		line.setItemName(item.getItemName());
		line.setSpecification(item.getSpecification());
		line.setUnit(item.getUnit());
		line.setQuantity(new BigDecimal("10"));
		line.setUnitPrice(new BigDecimal("1000"));

		order.getLines().add(line);

		return order;
	}

	// 元受注明細行に紐付けた売上を生成
	private SaleVO createLinkedSale(ItemVO item, WarehouseVO warehouse, Long orderLineId, String businessDate,
			String quantity) {

		SaleVO sale = new SaleVO();
		sale.setBusinessDate(businessDate);
		sale.setWarehouseId(warehouse.getWarehouseId());
		sale.setNote("未売上一覧テスト売上");

		SaleLineVO line = new SaleLineVO();
		line.setSalesOrderLineId(orderLineId);
		line.setItemId(item.getItemId());
		line.setItemName(item.getItemName());
		line.setSpecification(item.getSpecification());
		line.setUnit(item.getUnit());
		line.setQuantity(new BigDecimal(quantity));
		line.setUnitPrice(new BigDecimal("1000"));

		sale.getLines().add(line);

		return sale;
	}

	// 未売上一覧の明細行と合計を確認
	private void assertUnsoldReport(SalesOrderReportCriteria criteria, Long orderId, Long orderLineId,
			String remainingQuantity, String supplyAmount, String vatAmount) {

		SalesOrderReportVO report = service.getUnsold(criteria);

		assertNotNull(report);
		assertEquals(1, report.getMonthGroups().size());

		Map<String, Object> group = report.getMonthGroups().get(0);

		// 月別グループは元受注の日付で構成
		assertEquals("2026-01", group.get("monthLabel"));

		List<?> rows = (List<?>) group.get("rows");

		assertNotNull(rows);
		assertEquals(1, rows.size());

		SalesOrderReportRowVO row = (SalesOrderReportRowVO) rows.get(0);

		assertEquals(orderId, row.getSalesOrderId());
		assertEquals(orderLineId, row.getSalesOrderLineId());
		assertDecimal("10", row.getQuantity());
		assertDecimal(remainingQuantity, row.getRemainingQuantity());
		assertDecimal(supplyAmount, row.getRemainingSupplyAmount());
		assertDecimal(vatAmount, row.getRemainingVatAmount());

		Map<?, ?> monthTotals = (Map<?, ?>) group.get("totals");

		assertNotNull(monthTotals);
		assertDecimal("10", (BigDecimal) monthTotals.get("quantity"));
		assertDecimal(remainingQuantity, (BigDecimal) monthTotals.get("remainingQuantity"));
		assertDecimal(supplyAmount, (BigDecimal) monthTotals.get("remainingSupplyAmount"));
		assertDecimal(vatAmount, (BigDecimal) monthTotals.get("remainingVatAmount"));

		assertDecimal("10", report.getTotals().get("quantity"));
		assertDecimal(remainingQuantity, report.getTotals().get("remainingQuantity"));
		assertDecimal(supplyAmount, report.getTotals().get("remainingSupplyAmount"));
		assertDecimal(vatAmount, report.getTotals().get("remainingVatAmount"));
	}

	private void assertStockQuantity(WarehouseVO warehouse, ItemVO item, String expectedQuantity) {

		StockVO stock = stockMapper.read(warehouse.getWarehouseId(), item.getItemId());

		assertNotNull(stock);
		assertDecimal(expectedQuantity, stock.getQuantity());
	}

	private ItemVO createItem() {
		String token = createToken();

		ItemVO item = new ItemVO();
		item.setItemCode("SORTEST" + token);
		item.setItemName("未売上一覧テスト品目 " + token);
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
		warehouse.setWarehouseCode("SORWTEST" + token);
		warehouse.setWarehouseName("未売上一覧テスト倉庫 " + token);
		warehouse.setWarehouseType("倉庫");

		return warehouse;
	}

	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}

	private void assertDecimal(String expected, BigDecimal actual) {
		assertNotNull(actual);
		assertEquals(0, new BigDecimal(expected).compareTo(actual));
	}
}