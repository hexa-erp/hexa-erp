package hexa.erp.salesorder.service;

import static org.junit.Assert.assertEquals;
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
import hexa.erp.quotation.domain.QuotationLineVO;
import hexa.erp.quotation.domain.QuotationLookupLineVO;
import hexa.erp.quotation.domain.QuotationVO;
import hexa.erp.quotation.mapper.QuotationLookupMapper;
import hexa.erp.quotation.service.QuotationService;
import hexa.erp.salesorder.domain.SalesOrderLineVO;
import hexa.erp.salesorder.domain.SalesOrderVO;
import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.mapper.WarehouseMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
public class SalesOrderServiceTests {

	@Autowired
	private SalesOrderService service;

	@Autowired
	private ItemMapper itemMapper;

	@Autowired
	private WarehouseMapper warehouseMapper;

	@Autowired
	private QuotationService quotationService;

	@Autowired
	private QuotationLookupMapper quotationLookupMapper;

	// 주문서 저장·조회·수정과 금액 재계산 확인
	@Test
	public void testSaveAndUpdate() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 신규 저장: 수량 3, 단가 1000
		SalesOrderVO order = createOrder(item, warehouse, "3", "1000");

		Long orderId = service.save(order);

		assertNotNull(orderId);

		SalesOrderVO saved = service.get(orderId);

		assertNotNull(saved);
		assertEquals(1, saved.getLines().size());
		assertEquals(warehouse.getWarehouseName(), saved.getWarehouseName());
		assertEquals("IN_PROGRESS", saved.getProgressStatus());

		SalesOrderLineVO savedLine = saved.getLines().get(0);
		Long lineId = savedLine.getSalesOrderLineId();

		assertNotNull(lineId);
		assertEquals(0, new BigDecimal("3000").compareTo(savedLine.getSupplyAmount()));
		assertEquals(0, new BigDecimal("300").compareTo(savedLine.getVatAmount()));
		assertEquals(0, new BigDecimal("3300").compareTo(saved.getTotalAmount()));

		// 같은 주문서·상세행 수정: 수량 5, 단가 2000
		SalesOrderVO update = createOrder(item, warehouse, "5", "2000");

		update.setSalesOrderId(orderId);
		update.setNote("주문서 수정 테스트");
		update.getLines().get(0).setSalesOrderLineId(lineId);

		Long updatedId = service.save(update);
		SalesOrderVO changed = service.get(orderId);

		assertEquals(orderId, updatedId);
		assertNotNull(changed);
		assertEquals(saved.getSalesOrderNo(), changed.getSalesOrderNo());
		assertEquals("주문서 수정 테스트", changed.getNote());
		assertEquals(1, changed.getLines().size());

		SalesOrderLineVO changedLine = changed.getLines().get(0);

		assertEquals(lineId, changedLine.getSalesOrderLineId());
		assertEquals(0, new BigDecimal("5").compareTo(changedLine.getQuantity()));
		assertEquals(0, new BigDecimal("2000").compareTo(changedLine.getUnitPrice()));
		assertEquals(0, new BigDecimal("10000").compareTo(changedLine.getSupplyAmount()));
		assertEquals(0, new BigDecimal("1000").compareTo(changedLine.getVatAmount()));
		assertEquals(0, new BigDecimal("11000").compareTo(changedLine.getTotalAmount()));
		assertEquals(0, new BigDecimal("11000").compareTo(changed.getTotalAmount()));
	}

	// 주문서 상태 변경·수정 시 상태 유지·삭제 확인
	@Test
	public void testChangeStatusAndRemove() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		SalesOrderVO order = createOrder(item, warehouse, "3", "1000");

		Long orderId = service.save(order);
		List<Long> selectedIds = Collections.singletonList(orderId);

		// 진행상태를 완료로 변경
		int changeCount = service.changeStatus(selectedIds, "COMPLETED");

		SalesOrderVO completed = service.get(orderId);

		assertEquals(1, changeCount);
		assertNotNull(completed);
		assertEquals("COMPLETED", completed.getProgressStatus());
		assertEquals(1, completed.getLines().size());

		// 일반 수정 요청의 상태값으로 기존 완료 상태가 바뀌지 않는지 확인
		SalesOrderVO update = createOrder(item, warehouse, "3", "1000");

		update.setSalesOrderId(orderId);
		update.setProgressStatus("IN_PROGRESS");
		update.setNote("완료 상태 유지 테스트");
		update.getLines().get(0).setSalesOrderLineId(completed.getLines().get(0).getSalesOrderLineId());

		service.save(update);

		SalesOrderVO changed = service.get(orderId);

		assertNotNull(changed);
		assertEquals("완료 상태 유지 테스트", changed.getNote());
		assertEquals("COMPLETED", changed.getProgressStatus());

		// 전표 삭제 후 일반 조회에서 제외
		int removeCount = service.remove(selectedIds);

		assertEquals(1, removeCount);
		assertNull(service.get(orderId));

		// 이미 삭제된 전표는 다시 삭제하거나 상태를 변경할 수 없음
		assertEquals(0, service.remove(selectedIds));
		assertEquals(0, service.changeStatus(selectedIds, "IN_PROGRESS"));
	}

	// 원견적 연결과 주문 수정·삭제에 따른 잔량 확인
	@Test
	public void testQuotationLinkAndRemainingQuantity() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		String businessDate = LocalDate.now().toString();

		// 원견적 수량 10개 저장
		QuotationVO quotation = new QuotationVO();
		quotation.setBusinessDate(businessDate);
		quotation.setWarehouseId(warehouse.getWarehouseId());

		QuotationLineVO quotationLine = new QuotationLineVO();
		quotationLine.setItemId(item.getItemId());
		quotationLine.setItemName(item.getItemName());
		quotationLine.setSpecification(item.getSpecification());
		quotationLine.setUnit(item.getUnit());
		quotationLine.setQuantity(new BigDecimal("10"));
		quotationLine.setUnitPrice(new BigDecimal("1000"));

		quotation.getLines().add(quotationLine);

		Long quotationId = quotationService.save(quotation);
		Long quotationLineId = quotationLine.getQuotationLineId();

		assertNotNull(quotationId);
		assertNotNull(quotationLineId);
		assertRemainingQuantity(quotationId, "10");

		// 원견적 상세행을 연결해서 주문 3개 저장
		SalesOrderVO order = createOrder(item, warehouse, "3", "1000");

		order.setBusinessDate(businessDate);
		order.getLines().get(0).setQuotationLineId(quotationLineId);

		Long orderId = service.save(order);
		SalesOrderVO saved = service.get(orderId);

		assertNotNull(saved);
		assertEquals(1, saved.getLines().size());
		assertEquals(quotationLineId, saved.getLines().get(0).getQuotationLineId());
		assertRemainingQuantity(quotationId, "7");

		// 같은 주문 상세행의 수량을 5개로 수정
		saved.getLines().get(0).setQuantity(new BigDecimal("5"));
		service.save(saved);

		SalesOrderVO changed = service.get(orderId);

		assertNotNull(changed);
		assertEquals(1, changed.getLines().size());
		assertEquals(quotationLineId, changed.getLines().get(0).getQuotationLineId());
		assertRemainingQuantity(quotationId, "5");

		// 주문 삭제 후 원견적 잔량이 10개로 복원
		assertEquals(1, service.remove(Collections.singletonList(orderId)));
		assertNull(service.get(orderId));
		assertRemainingQuantity(quotationId, "10");
	}

	// 원본 전표 선택 조회에서 사용하는 잔량 확인
	private void assertRemainingQuantity(Long quotationId, String expectedQuantity) {

		List<QuotationLookupLineVO> lines = quotationLookupMapper.getLines(quotationId);

		assertEquals(1, lines.size());
		assertEquals(0, new BigDecimal(expectedQuantity).compareTo(lines.get(0).getRemainingQuantity()));
	}

	// 테스트 주문서 생성
	private SalesOrderVO createOrder(ItemVO item, WarehouseVO warehouse, String quantity, String unitPrice) {

		SalesOrderVO order = new SalesOrderVO();
		order.setBusinessDate(LocalDate.now().toString());
		order.setWarehouseId(warehouse.getWarehouseId());
		order.setNote("주문서 Service 테스트");

		SalesOrderLineVO line = new SalesOrderLineVO();
		line.setItemId(item.getItemId());
		line.setItemName(item.getItemName());
		line.setSpecification(item.getSpecification());
		line.setUnit(item.getUnit());
		line.setQuantity(new BigDecimal(quantity));
		line.setUnitPrice(new BigDecimal(unitPrice));

		order.getLines().add(line);

		return order;
	}

	// 테스트 품목 생성
	private ItemVO createItem() {
		String token = createToken();

		ItemVO item = new ItemVO();
		item.setItemCode("OTEST" + token);
		item.setItemName("주문 테스트 품목 " + token);
		item.setSpecification("100mm");
		item.setUnit("EA");
		item.setItemType("상품");
		item.setInboundPrice(BigDecimal.ZERO);
		item.setOutboundPrice(new BigDecimal("1000"));

		return item;
	}

	// 테스트 창고 생성
	private WarehouseVO createWarehouse() {
		String token = createToken();

		WarehouseVO warehouse = new WarehouseVO();
		warehouse.setWarehouseCode("OWTEST" + token);
		warehouse.setWarehouseName("주문 테스트 창고 " + token);
		warehouse.setWarehouseType("창고");

		return warehouse;
	}

	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}