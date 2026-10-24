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

	// 미판매 현황의 기준일, 판매 수정·삭제와 잔량·재고 반영 확인
	@Test
	public void testUnsoldCutoffDateAndSaleChanges() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 원주문 10개 저장
		SalesOrderVO order = createOrder(item, warehouse);
		Long orderId = salesOrderService.save(order);
		Long orderLineId = order.getLines().get(0).getSalesOrderLineId();

		assertNotNull(orderId);
		assertNotNull(orderLineId);

		// 1월 31일 판매 3개
		SaleVO januarySale = createLinkedSale(item, warehouse, orderLineId, "2026-01-31", "3");

		Long januarySaleId = saleService.save(januarySale);

		assertNotNull(januarySaleId);
		assertEquals(orderLineId, januarySale.getLines().get(0).getSalesOrderLineId());

		// 2월 1일 판매 4개
		SaleVO februarySale = createLinkedSale(item, warehouse, orderLineId, "2026-02-01", "4");

		Long februarySaleId = saleService.save(februarySale);

		assertNotNull(februarySaleId);
		assertEquals(orderLineId, februarySale.getLines().get(0).getSalesOrderLineId());
		assertStockQuantity(warehouse, item, "-7");

		SalesOrderReportCriteria criteria = new SalesOrderReportCriteria();
		criteria.setItemIds(Collections.singletonList(item.getItemId().toString()));

		// 1월 31일 기준: 판매 3개만 차감 → 잔량 7
		criteria.setCutoffDate("2026-01-31");
		assertUnsoldReport(criteria, orderId, orderLineId, "7", "7000", "700");
		assertStockQuantity(warehouse, item, "-7");

		// 2월 1일 기준: 판매 3개와 4개 차감 → 잔량 3
		criteria.setCutoffDate("2026-02-01");
		assertUnsoldReport(criteria, orderId, orderLineId, "3", "3000", "300");
		assertStockQuantity(warehouse, item, "-7");

		// 기존 판매 상세행 ID 확인
		SaleVO existingSale = saleService.get(februarySaleId);

		assertNotNull(existingSale);
		assertEquals(1, existingSale.getLines().size());

		Long saleLineId = existingSale.getLines().get(0).getSaleLineId();

		// 수정 요청은 별도 VO로 구성: 판매 수량 4 → 5
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

		// 판매 합계 8개 → 잔량 2, 재고 -8
		assertUnsoldReport(criteria, orderId, orderLineId, "2", "2000", "200");
		assertStockQuantity(warehouse, item, "-8");

		// 2월 1일 판매 삭제 → 1월 31일 판매 3개만 유지
		assertEquals(1, saleService.remove(Collections.singletonList(februarySaleId)));
		assertNull(saleService.get(februarySaleId));

		assertUnsoldReport(criteria, orderId, orderLineId, "7", "7000", "700");
		assertStockQuantity(warehouse, item, "-3");
	}

	// 원주문 10개 생성
	private SalesOrderVO createOrder(ItemVO item, WarehouseVO warehouse) {

		SalesOrderVO order = new SalesOrderVO();
		order.setBusinessDate("2026-01-01");
		order.setWarehouseId(warehouse.getWarehouseId());
		order.setNote("미판매 현황 원주문 테스트");

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

	// 원주문 상세행을 연결한 판매 생성
	private SaleVO createLinkedSale(ItemVO item, WarehouseVO warehouse, Long orderLineId, String businessDate,
			String quantity) {

		SaleVO sale = new SaleVO();
		sale.setBusinessDate(businessDate);
		sale.setWarehouseId(warehouse.getWarehouseId());
		sale.setNote("미판매 현황 테스트 판매");

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

	// 미판매 현황의 상세행과 합계 확인
	private void assertUnsoldReport(SalesOrderReportCriteria criteria, Long orderId, Long orderLineId,
			String remainingQuantity, String supplyAmount, String vatAmount) {

		SalesOrderReportVO report = service.getUnsold(criteria);

		assertNotNull(report);
		assertEquals(1, report.getMonthGroups().size());

		Map<String, Object> group = report.getMonthGroups().get(0);

		// 월별 그룹은 원주문 일자를 기준으로 구성
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
		item.setItemName("미판매 현황 테스트 품목 " + token);
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
		warehouse.setWarehouseCode("SORWTEST" + token);
		warehouse.setWarehouseName("미판매 현황 테스트 창고 " + token);
		warehouse.setWarehouseType("창고");

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