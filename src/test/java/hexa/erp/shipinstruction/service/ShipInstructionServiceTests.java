package hexa.erp.shipinstruction.service;

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
import hexa.erp.sale.domain.SaleLineVO;
import hexa.erp.sale.domain.SaleLookupLineVO;
import hexa.erp.sale.domain.SaleVO;
import hexa.erp.sale.mapper.SaleLookupMapper;
import hexa.erp.sale.service.SaleService;
import hexa.erp.shipinstruction.domain.ShipInstructionLineVO;
import hexa.erp.shipinstruction.domain.ShipInstructionVO;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.stock.mapper.StockMapper;
import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.mapper.WarehouseMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
public class ShipInstructionServiceTests {

	@Autowired
	private ShipInstructionService service;

	@Autowired
	private SaleService saleService;

	@Autowired
	private ItemMapper itemMapper;

	@Autowired
	private WarehouseMapper warehouseMapper;

	@Autowired
	private StockMapper stockMapper;

	@Autowired
	private SaleLookupMapper saleLookupMapper;

	// 出荷指示の保存・更新、元売上との紐付け、在庫保持を確認
	@Test
	public void testSaveAndUpdateWithoutChangingStock() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 売上3個を保存：この時点でのみ在庫を減算
		SaleVO sale = createSale(item, warehouse);
		Long saleId = saleService.save(sale);

		assertNotNull(saleId);

		Long saleLineId = sale.getLines().get(0).getSaleLineId();

		assertNotNull(saleLineId);
		assertStockQuantity(warehouse, item, "-3");

		// 元売上明細行に紐付けて出荷指示2個を保存
		ShipInstructionVO instruction = createInstruction(item, warehouse, saleLineId, "2");

		Long instructionId = service.save(instruction);
		ShipInstructionVO saved = service.get(instructionId);

		assertNotNull(instructionId);
		assertNotNull(saved);
		assertEquals(1, saved.getLines().size());
		assertEquals("IN_PROGRESS", saved.getProgressStatus());
		assertEquals(warehouse.getWarehouseName(), saved.getWarehouseName());

		ShipInstructionLineVO savedLine = saved.getLines().get(0);
		Long lineId = savedLine.getShipInstructionLineId();

		assertNotNull(lineId);
		assertEquals(saleLineId, savedLine.getSaleLineId());
		assertEquals(0, new BigDecimal("2").compareTo(savedLine.getQuantity()));
		assertEquals(0, new BigDecimal("2").compareTo(saved.getTotalQuantity()));
		assertStockQuantity(warehouse, item, "-3");

		// 同じ出荷指示明細行の数量を1個に変更
		ShipInstructionVO update = createInstruction(item, warehouse, saleLineId, "1");

		update.setShipInstructionId(instructionId);
		update.setNote("出荷指示の更新テスト");
		update.getLines().get(0).setShipInstructionLineId(lineId);

		Long updatedId = service.save(update);
		ShipInstructionVO changed = service.get(instructionId);

		assertEquals(instructionId, updatedId);
		assertNotNull(changed);
		assertEquals(saved.getShipInstructionNo(), changed.getShipInstructionNo());
		assertEquals("出荷指示の更新テスト", changed.getNote());
		assertEquals(1, changed.getLines().size());

		ShipInstructionLineVO changedLine = changed.getLines().get(0);

		assertEquals(lineId, changedLine.getShipInstructionLineId());
		assertEquals(saleLineId, changedLine.getSaleLineId());
		assertEquals(0, new BigDecimal("1").compareTo(changedLine.getQuantity()));
		assertEquals(0, new BigDecimal("1").compareTo(changed.getTotalQuantity()));
		assertStockQuantity(warehouse, item, "-3");
	}

	// 出荷指示の状態変更・削除と在庫保持を確認
	@Test
	public void testChangeStatusAndRemoveWithoutChangingStock() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 元売上を保存：在庫-3
		SaleVO sale = createSale(item, warehouse);
		saleService.save(sale);

		Long saleLineId = sale.getLines().get(0).getSaleLineId();

		assertNotNull(saleLineId);
		assertStockQuantity(warehouse, item, "-3");

		ShipInstructionVO instruction = createInstruction(item, warehouse, saleLineId, "2");

		Long instructionId = service.save(instruction);

		assertNotNull(instructionId);
		assertStockQuantity(warehouse, item, "-3");

		List<Long> selectedIds = Collections.singletonList(instructionId);

		// 進行状況を完了に変更
		int changeCount = service.changeStatus(selectedIds, "COMPLETED");
		ShipInstructionVO completed = service.get(instructionId);

		assertEquals(1, changeCount);
		assertNotNull(completed);
		assertEquals("COMPLETED", completed.getProgressStatus());
		assertEquals(1, completed.getLines().size());
		assertStockQuantity(warehouse, item, "-3");

		// 通常の更新で完了状態が初期化されないことを確認
		ShipInstructionVO update = createInstruction(item, warehouse, saleLineId, "2");

		update.setShipInstructionId(instructionId);
		update.setProgressStatus("IN_PROGRESS");
		update.setNote("出荷指示の完了状態保持テスト");
		update.getLines().get(0).setShipInstructionLineId(completed.getLines().get(0).getShipInstructionLineId());

		service.save(update);

		ShipInstructionVO changed = service.get(instructionId);

		assertNotNull(changed);
		assertEquals("出荷指示の完了状態保持テスト", changed.getNote());
		assertEquals("COMPLETED", changed.getProgressStatus());
		assertStockQuantity(warehouse, item, "-3");

		// 指示書削除後も元売上と在庫を保持
		assertEquals(1, service.remove(selectedIds));
		assertNull(service.get(instructionId));
		assertNotNull(saleService.get(sale.getSaleId()));
		assertStockQuantity(warehouse, item, "-3");

		// 削除済み指示書は再削除や状態変更ができない
		assertEquals(0, service.remove(selectedIds));
		assertEquals(0, service.changeStatus(selectedIds, "IN_PROGRESS"));
		assertStockQuantity(warehouse, item, "-3");
	}

	// 元売上との紐付けと出荷指示更新・削除後の未指示残数を確認
	@Test
	public void testSaleLinkAndRemainingQuantity() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 元売上3個を保存
		SaleVO sale = createSale(item, warehouse);
		Long saleId = saleService.save(sale);
		Long saleLineId = sale.getLines().get(0).getSaleLineId();

		assertNotNull(saleId);
		assertNotNull(saleLineId);
		assertRemainingQuantity(saleId, "3");
		assertStockQuantity(warehouse, item, "-3");

		// 出荷指示2個を保存 → 未指示残数1個
		ShipInstructionVO instruction = createInstruction(item, warehouse, saleLineId, "2");

		Long instructionId = service.save(instruction);
		ShipInstructionVO saved = service.get(instructionId);

		assertNotNull(saved);
		assertEquals(1, saved.getLines().size());
		assertEquals(saleLineId, saved.getLines().get(0).getSaleLineId());
		assertRemainingQuantity(saleId, "1");
		assertStockQuantity(warehouse, item, "-3");

		// 完了状態でも指示済み数量は残数計算に含める
		assertEquals(1, service.changeStatus(Collections.singletonList(instructionId), "COMPLETED"));
		assertRemainingQuantity(saleId, "1");

		// 同じ指示行の数量を1個に変更 → 未指示残数2個
		saved.getLines().get(0).setQuantity(new BigDecimal("1"));
		service.save(saved);

		ShipInstructionVO changed = service.get(instructionId);

		assertNotNull(changed);
		assertEquals(1, changed.getLines().size());
		assertEquals(saleLineId, changed.getLines().get(0).getSaleLineId());
		assertRemainingQuantity(saleId, "2");
		assertStockQuantity(warehouse, item, "-3");

		// 指示書を削除 → 未指示残数3個へ復元
		assertEquals(1, service.remove(Collections.singletonList(instructionId)));
		assertNull(service.get(instructionId));
		assertRemainingQuantity(saleId, "3");
		assertStockQuantity(warehouse, item, "-3");
	}

	// 元伝票選択で使用する未指示残数を確認
	private void assertRemainingQuantity(Long saleId, String expectedQuantity) {

		List<SaleLookupLineVO> lines = saleLookupMapper.getLines(saleId);

		assertEquals(1, lines.size());
		assertEquals(0, new BigDecimal(expectedQuantity).compareTo(lines.get(0).getRemainingQuantity()));
	}

	// テスト出荷指示書を生成
	private ShipInstructionVO createInstruction(ItemVO item, WarehouseVO warehouse, Long saleLineId, String quantity) {

		ShipInstructionVO instruction = new ShipInstructionVO();
		instruction.setBusinessDate(LocalDate.now().toString());
		instruction.setWarehouseId(warehouse.getWarehouseId());
		instruction.setNote("出荷指示Serviceテスト");

		ShipInstructionLineVO line = new ShipInstructionLineVO();
		line.setItemId(item.getItemId());
		line.setSaleLineId(saleLineId);
		line.setItemName(item.getItemName());
		line.setSpecification(item.getSpecification());
		line.setUnit(item.getUnit());
		line.setQuantity(new BigDecimal(quantity));

		instruction.getLines().add(line);

		return instruction;
	}

	// 元伝票に使用するテスト売上を生成
	private SaleVO createSale(ItemVO item, WarehouseVO warehouse) {
		SaleVO sale = new SaleVO();
		sale.setBusinessDate(LocalDate.now().toString());
		sale.setWarehouseId(warehouse.getWarehouseId());
		sale.setNote("出荷指示の元売上テスト");

		SaleLineVO line = new SaleLineVO();
		line.setItemId(item.getItemId());
		line.setItemName(item.getItemName());
		line.setSpecification(item.getSpecification());
		line.setUnit(item.getUnit());
		line.setQuantity(new BigDecimal("3"));
		line.setUnitPrice(new BigDecimal("1000"));

		sale.getLines().add(line);

		return sale;
	}

	// 現在の在庫を確認
	private void assertStockQuantity(WarehouseVO warehouse, ItemVO item, String expectedQuantity) {

		StockVO stock = stockMapper.read(warehouse.getWarehouseId(), item.getItemId());

		assertNotNull(stock);
		assertEquals(0, new BigDecimal(expectedQuantity).compareTo(stock.getQuantity()));
	}

	// テスト品目を生成
	private ItemVO createItem() {
		String token = createToken();

		ItemVO item = new ItemVO();
		item.setItemCode("SITEST" + token);
		item.setItemName("出荷指示テスト品目 " + token);
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
		warehouse.setWarehouseCode("SIWTEST" + token);
		warehouse.setWarehouseName("出荷指示テスト倉庫 " + token);
		warehouse.setWarehouseType("倉庫");

		return warehouse;
	}

	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}