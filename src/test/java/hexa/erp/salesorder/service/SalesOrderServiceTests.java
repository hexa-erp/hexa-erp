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

	// 受注伝票の保存・取得・更新と金額再計算を確認
	@Test
	public void testSaveAndUpdate() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 新規保存：数量3、単価1000
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

		// 同じ受注伝票・明細行を更新：数量5、単価2000
		SalesOrderVO update = createOrder(item, warehouse, "5", "2000");

		update.setSalesOrderId(orderId);
		update.setNote("受注伝票の更新テスト");
		update.getLines().get(0).setSalesOrderLineId(lineId);

		Long updatedId = service.save(update);
		SalesOrderVO changed = service.get(orderId);

		assertEquals(orderId, updatedId);
		assertNotNull(changed);
		assertEquals(saved.getSalesOrderNo(), changed.getSalesOrderNo());
		assertEquals("受注伝票の更新テスト", changed.getNote());
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

	// 受注伝票の状態変更・更新時の状態保持・削除を確認
	@Test
	public void testChangeStatusAndRemove() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		SalesOrderVO order = createOrder(item, warehouse, "3", "1000");

		Long orderId = service.save(order);
		List<Long> selectedIds = Collections.singletonList(orderId);

		// 進行状況を完了に変更
		int changeCount = service.changeStatus(selectedIds, "COMPLETED");

		SalesOrderVO completed = service.get(orderId);

		assertEquals(1, changeCount);
		assertNotNull(completed);
		assertEquals("COMPLETED", completed.getProgressStatus());
		assertEquals(1, completed.getLines().size());

		// 通常の更新リクエストの状態値で既存の完了状態が変わらないことを確認
		SalesOrderVO update = createOrder(item, warehouse, "3", "1000");

		update.setSalesOrderId(orderId);
		update.setProgressStatus("IN_PROGRESS");
		update.setNote("完了状態の保持テスト");
		update.getLines().get(0).setSalesOrderLineId(completed.getLines().get(0).getSalesOrderLineId());

		service.save(update);

		SalesOrderVO changed = service.get(orderId);

		assertNotNull(changed);
		assertEquals("完了状態の保持テスト", changed.getNote());
		assertEquals("COMPLETED", changed.getProgressStatus());

		// 伝票削除後は通常の取得から除外
		int removeCount = service.remove(selectedIds);

		assertEquals(1, removeCount);
		assertNull(service.get(orderId));

		// 削除済み伝票は再削除や状態変更ができない
		assertEquals(0, service.remove(selectedIds));
		assertEquals(0, service.changeStatus(selectedIds, "IN_PROGRESS"));
	}

	// 元見積との紐付けと受注更新・削除後の残数を確認
	@Test
	public void testQuotationLinkAndRemainingQuantity() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		String businessDate = LocalDate.now().toString();

		// 元見積数量10個を保存
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

		// 元見積明細行に紐付けて受注3個を保存
		SalesOrderVO order = createOrder(item, warehouse, "3", "1000");

		order.setBusinessDate(businessDate);
		order.getLines().get(0).setQuotationLineId(quotationLineId);

		Long orderId = service.save(order);
		SalesOrderVO saved = service.get(orderId);

		assertNotNull(saved);
		assertEquals(1, saved.getLines().size());
		assertEquals(quotationLineId, saved.getLines().get(0).getQuotationLineId());
		assertRemainingQuantity(quotationId, "7");

		// 同じ受注明細行の数量を5個に変更
		saved.getLines().get(0).setQuantity(new BigDecimal("5"));
		service.save(saved);

		SalesOrderVO changed = service.get(orderId);

		assertNotNull(changed);
		assertEquals(1, changed.getLines().size());
		assertEquals(quotationLineId, changed.getLines().get(0).getQuotationLineId());
		assertRemainingQuantity(quotationId, "5");

		// 受注削除後に元見積の残数が10個へ復元
		assertEquals(1, service.remove(Collections.singletonList(orderId)));
		assertNull(service.get(orderId));
		assertRemainingQuantity(quotationId, "10");
	}

	// 元伝票選択で使用する残数を確認
	private void assertRemainingQuantity(Long quotationId, String expectedQuantity) {

		List<QuotationLookupLineVO> lines = quotationLookupMapper.getLines(quotationId);

		assertEquals(1, lines.size());
		assertEquals(0, new BigDecimal(expectedQuantity).compareTo(lines.get(0).getRemainingQuantity()));
	}

	// テスト受注伝票を生成
	private SalesOrderVO createOrder(ItemVO item, WarehouseVO warehouse, String quantity, String unitPrice) {

		SalesOrderVO order = new SalesOrderVO();
		order.setBusinessDate(LocalDate.now().toString());
		order.setWarehouseId(warehouse.getWarehouseId());
		order.setNote("受注伝票Serviceテスト");

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

	// テスト品目を生成
	private ItemVO createItem() {
		String token = createToken();

		ItemVO item = new ItemVO();
		item.setItemCode("OTEST" + token);
		item.setItemName("受注テスト品目 " + token);
		item.setSpecification("100mm");
		item.setUnit("EA");
		item.setItemType("商品");
		item.setInboundPrice(BigDecimal.ZERO);
		item.setOutboundPrice(new BigDecimal("1000"));

		return item;
	}

	// テスト倉庫を生成
	private WarehouseVO createWarehouse() {
		String token = createToken();

		WarehouseVO warehouse = new WarehouseVO();
		warehouse.setWarehouseCode("OWTEST" + token);
		warehouse.setWarehouseName("受注テスト倉庫 " + token);
		warehouse.setWarehouseType("倉庫");

		return warehouse;
	}

	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}