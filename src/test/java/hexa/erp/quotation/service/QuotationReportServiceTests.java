package hexa.erp.quotation.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import hexa.erp.quotation.domain.QuotationLineVO;
import hexa.erp.quotation.domain.QuotationReportCriteria;
import hexa.erp.quotation.domain.QuotationReportRowVO;
import hexa.erp.quotation.domain.QuotationReportVO;
import hexa.erp.quotation.domain.QuotationVO;
import hexa.erp.salesorder.domain.SalesOrderLineVO;
import hexa.erp.salesorder.domain.SalesOrderVO;
import hexa.erp.salesorder.service.SalesOrderService;
import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.mapper.WarehouseMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
public class QuotationReportServiceTests {

	@Autowired
	private QuotationReportService service;

	@Autowired
	private QuotationService quotationService;

	@Autowired
	private ItemMapper itemMapper;

	@Autowired
	private SalesOrderService salesOrderService;

	@Autowired
	private WarehouseMapper warehouseMapper;

	// 시작일·종료일 포함 및 범위 밖 자료 제외 확인
	@Test
	public void testDateRangeIncludesOnlySelectedDay() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		quotationService.save(createQuotation(item, "2026-01-09", "1"));

		QuotationVO target = createQuotation(item, "2026-01-10", "2");
		Long targetId = quotationService.save(target);

		quotationService.save(createQuotation(item, "2026-01-11", "4"));

		assertNotNull(targetId);

		QuotationReportCriteria criteria = new QuotationReportCriteria();
		criteria.setStartDate("2026-01-10");
		criteria.setEndDate("2026-01-10");

		// 이번 테스트에서 만든 품목으로 검색 범위 제한
		criteria.setItemIds(Collections.singletonList(item.getItemId().toString()));

		QuotationReportVO report = service.getStatus(criteria);

		assertNotNull(report);
		assertEquals(1, report.getMonthGroups().size());

		Map<String, Object> group = report.getMonthGroups().get(0);

		assertEquals("2026-01", group.get("monthLabel"));

		List<?> rows = (List<?>) group.get("rows");

		assertNotNull(rows);
		assertEquals(1, rows.size());

		QuotationReportRowVO row = (QuotationReportRowVO) rows.get(0);

		assertEquals(targetId, row.getQuotationId());
		assertEquals(target.getLines().get(0).getQuotationLineId(), row.getQuotationLineId());
		assertEquals("2026-01-10", row.getBusinessDate());
		assertDecimal("2", row.getQuantity());
		assertDecimal("2000", row.getSupplyAmount());

		// 조회된 자료의 월 소계 확인
		Map<?, ?> monthTotals = (Map<?, ?>) group.get("totals");

		assertNotNull(monthTotals);
		assertDecimal("2", (BigDecimal) monthTotals.get("quantity"));
		assertDecimal("1000", (BigDecimal) monthTotals.get("unitPrice"));
		assertDecimal("2000", (BigDecimal) monthTotals.get("supplyAmount"));

		// 전체 합계도 해당 날짜의 자료만 포함
		assertDecimal("2", report.getTotals().get("quantity"));
		assertDecimal("1000", report.getTotals().get("unitPrice"));
		assertDecimal("2000", report.getTotals().get("supplyAmount"));
	}

	// 미주문 현황의 기준일 적용과 주문 삭제 후 잔량 복원 확인
	@Test
	public void testUnorderedCutoffDateAndDeletedOrder() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		QuotationVO quotation = createQuotation(item, "2026-01-01", "10");
		quotation.setWarehouseId(warehouse.getWarehouseId());

		Long quotationId = quotationService.save(quotation);
		Long quotationLineId = quotation.getLines().get(0).getQuotationLineId();

		assertNotNull(quotationId);
		assertNotNull(quotationLineId);

		SalesOrderVO januaryOrder = createLinkedOrder(item, warehouse, quotationLineId, "2026-01-31", "3");

		assertNotNull(salesOrderService.save(januaryOrder));

		SalesOrderVO februaryOrder = createLinkedOrder(item, warehouse, quotationLineId, "2026-02-01", "4");

		Long februaryOrderId = salesOrderService.save(februaryOrder);

		assertNotNull(februaryOrderId);

		QuotationReportCriteria criteria = new QuotationReportCriteria();
		criteria.setItemIds(Collections.singletonList(item.getItemId().toString()));

		// 기준일 다음 날의 주문은 차감하지 않음
		criteria.setCutoffDate("2026-01-31");
		assertUnorderedReport(criteria, quotationId, quotationLineId, "7", "7000", "700");

		// 기준일 당일 주문까지 포함
		criteria.setCutoffDate("2026-02-01");
		assertUnorderedReport(criteria, quotationId, quotationLineId, "3", "3000", "300");

		// 2월 1일 주문 삭제 후 잔량 복원
		assertEquals(1, salesOrderService.remove(Collections.singletonList(februaryOrderId)));

		assertUnorderedReport(criteria, quotationId, quotationLineId, "7", "7000", "700");
	}

	// 원견적 상세행을 연결한 주문서 생성
	private SalesOrderVO createLinkedOrder(ItemVO item, WarehouseVO warehouse, Long quotationLineId,
			String businessDate, String quantity) {

		SalesOrderVO order = new SalesOrderVO();
		order.setBusinessDate(businessDate);
		order.setWarehouseId(warehouse.getWarehouseId());
		order.setNote("미주문 현황 테스트 주문");

		SalesOrderLineVO line = new SalesOrderLineVO();
		line.setQuotationLineId(quotationLineId);
		line.setItemId(item.getItemId());
		line.setItemName(item.getItemName());
		line.setSpecification(item.getSpecification());
		line.setUnit(item.getUnit());
		line.setQuantity(new BigDecimal(quantity));
		line.setUnitPrice(new BigDecimal("1000"));

		order.getLines().add(line);

		return order;
	}

	private WarehouseVO createWarehouse() {
		String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		WarehouseVO warehouse = new WarehouseVO();
		warehouse.setWarehouseCode("QRWTEST" + token);
		warehouse.setWarehouseName("미주문 현황 테스트 창고 " + token);
		warehouse.setWarehouseType("창고");

		return warehouse;
	}

	// 미주문 현황의 상세행과 전체 합계 확인
	private void assertUnorderedReport(QuotationReportCriteria criteria, Long quotationId, Long quotationLineId,
			String remainingQuantity, String supplyAmount, String vatAmount) {

		QuotationReportVO report = service.getUnordered(criteria);

		assertNotNull(report);
		assertEquals(1, report.getMonthGroups().size());

		Map<String, Object> group = report.getMonthGroups().get(0);

		// 월별 그룹은 원견적 일자를 기준으로 구성
		assertEquals("2026-01", group.get("monthLabel"));

		List<?> rows = (List<?>) group.get("rows");

		assertNotNull(rows);
		assertEquals(1, rows.size());

		QuotationReportRowVO row = (QuotationReportRowVO) rows.get(0);

		assertEquals(quotationId, row.getQuotationId());
		assertEquals(quotationLineId, row.getQuotationLineId());
		assertDecimal("10", row.getQuantity());
		assertDecimal(remainingQuantity, row.getRemainingQuantity());
		assertDecimal(supplyAmount, row.getRemainingSupplyAmount());
		assertDecimal(vatAmount, row.getRemainingVatAmount());

		assertDecimal("10", report.getTotals().get("quantity"));
		assertDecimal(remainingQuantity, report.getTotals().get("remainingQuantity"));
		assertDecimal(supplyAmount, report.getTotals().get("remainingSupplyAmount"));
		assertDecimal(vatAmount, report.getTotals().get("remainingVatAmount"));
	}

	// 지정한 날짜와 수량으로 견적서 생성
	private QuotationVO createQuotation(ItemVO item, String businessDate, String quantity) {

		QuotationVO quotation = new QuotationVO();
		quotation.setBusinessDate(businessDate);
		quotation.setNote("견적 현황 테스트");

		QuotationLineVO line = new QuotationLineVO();
		line.setItemId(item.getItemId());
		line.setItemName(item.getItemName());
		line.setSpecification(item.getSpecification());
		line.setUnit(item.getUnit());
		line.setQuantity(new BigDecimal(quantity));
		line.setUnitPrice(new BigDecimal("1000"));

		quotation.getLines().add(line);

		return quotation;
	}

	private ItemVO createItem() {
		String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		ItemVO item = new ItemVO();
		item.setItemCode("QRTEST" + token);
		item.setItemName("견적 현황 테스트 품목 " + token);
		item.setSpecification("100mm");
		item.setUnit("EA");
		item.setItemType("상품");
		item.setInboundPrice(BigDecimal.ZERO);
		item.setOutboundPrice(new BigDecimal("1000"));

		return item;
	}

	private void assertDecimal(String expected, BigDecimal actual) {
		assertNotNull(actual);
		assertEquals(0, new BigDecimal(expected).compareTo(actual));
	}
}