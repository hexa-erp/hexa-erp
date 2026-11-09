package hexa.erp.shipment.service;

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
import hexa.erp.sale.domain.SaleVO;
import hexa.erp.sale.service.SaleService;
import hexa.erp.shipinstruction.domain.ShipInstructionLineVO;
import hexa.erp.shipinstruction.domain.ShipInstructionLookupLineVO;
import hexa.erp.shipinstruction.domain.ShipInstructionVO;
import hexa.erp.shipinstruction.mapper.ShipInstructionLookupMapper;
import hexa.erp.shipinstruction.service.ShipInstructionService;
import hexa.erp.shipment.domain.ShipmentLineVO;
import hexa.erp.shipment.domain.ShipmentVO;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.stock.mapper.StockMapper;
import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.mapper.WarehouseMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
public class ShipmentServiceTests {

	@Autowired
	private ShipmentService service;

	@Autowired
	private ShipInstructionService shipInstructionService;

	@Autowired
	private SaleService saleService;

	@Autowired
	private ItemMapper itemMapper;

	@Autowired
	private WarehouseMapper warehouseMapper;

	@Autowired
	private StockMapper stockMapper;

	@Autowired
	private ShipInstructionLookupMapper shipInstructionLookupMapper;

	// 出荷の保存・更新、元出荷指示との紐付け、在庫保持を確認
	@Test
	public void testSaveAndUpdateWithoutChangingStock() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 元売上3個と出荷指示2個を保存
		ShipInstructionVO instruction = saveInstruction(item, warehouse);

		assertNotNull(instruction);
		assertEquals(1, instruction.getLines().size());

		Long instructionLineId = instruction.getLines().get(0).getShipInstructionLineId();

		assertNotNull(instructionLineId);
		assertStockQuantity(warehouse, item, "-3");

		// 元出荷指示明細行に紐付けて出荷1個を保存
		ShipmentVO shipment = createShipment(item, warehouse, instructionLineId, "1");

		Long shipmentId = service.save(shipment);
		ShipmentVO saved = service.get(shipmentId);

		assertNotNull(shipmentId);
		assertNotNull(saved);
		assertEquals(1, saved.getLines().size());
		assertEquals("CONFIRMED", saved.getProgressStatus());
		assertEquals(warehouse.getWarehouseName(), saved.getWarehouseName());

		ShipmentLineVO savedLine = saved.getLines().get(0);
		Long lineId = savedLine.getShipmentLineId();

		assertNotNull(lineId);
		assertEquals(instructionLineId, savedLine.getShipInstructionLineId());
		assertEquals(0, new BigDecimal("1").compareTo(savedLine.getQuantity()));
		assertEquals(0, new BigDecimal("1").compareTo(saved.getTotalQuantity()));
		assertStockQuantity(warehouse, item, "-3");

		// 同じ出荷明細行の数量を2個に変更
		ShipmentVO update = createShipment(item, warehouse, instructionLineId, "2");

		update.setShipmentId(shipmentId);
		update.setNote("出荷の更新テスト");
		update.getLines().get(0).setShipmentLineId(lineId);

		Long updatedId = service.save(update);
		ShipmentVO changed = service.get(shipmentId);

		assertEquals(shipmentId, updatedId);
		assertNotNull(changed);
		assertEquals(saved.getShipmentNo(), changed.getShipmentNo());
		assertEquals("出荷の更新テスト", changed.getNote());
		assertEquals(1, changed.getLines().size());

		ShipmentLineVO changedLine = changed.getLines().get(0);

		assertEquals(lineId, changedLine.getShipmentLineId());
		assertEquals(instructionLineId, changedLine.getShipInstructionLineId());
		assertEquals(0, new BigDecimal("2").compareTo(changedLine.getQuantity()));
		assertEquals(0, new BigDecimal("2").compareTo(changed.getTotalQuantity()));
		assertStockQuantity(warehouse, item, "-3");
	}

	// 出荷の状態変更・削除と在庫保持を確認
	@Test
	public void testChangeStatusAndRemoveWithoutChangingStock() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		ShipInstructionVO instruction = saveInstruction(item, warehouse);

		assertNotNull(instruction);
		assertEquals(1, instruction.getLines().size());

		Long instructionLineId = instruction.getLines().get(0).getShipInstructionLineId();

		ShipmentVO shipment = createShipment(item, warehouse, instructionLineId, "1");

		Long shipmentId = service.save(shipment);

		assertNotNull(shipmentId);
		assertStockQuantity(warehouse, item, "-3");

		List<Long> selectedIds = Collections.singletonList(shipmentId);

		// 進行状況を未確認に変更
		int changeCount = service.changeStatus(selectedIds, "UNCONFIRMED");
		ShipmentVO unconfirmed = service.get(shipmentId);

		assertEquals(1, changeCount);
		assertNotNull(unconfirmed);
		assertEquals("UNCONFIRMED", unconfirmed.getProgressStatus());
		assertEquals(1, unconfirmed.getLines().size());
		assertStockQuantity(warehouse, item, "-3");

		// 通常の更新で未確認状態が初期化されないことを確認
		ShipmentVO update = createShipment(item, warehouse, instructionLineId, "1");

		update.setShipmentId(shipmentId);
		update.setProgressStatus("CONFIRMED");
		update.setNote("出荷の未確認状態保持テスト");
		update.getLines().get(0).setShipmentLineId(unconfirmed.getLines().get(0).getShipmentLineId());

		service.save(update);

		ShipmentVO changed = service.get(shipmentId);

		assertNotNull(changed);
		assertEquals("出荷の未確認状態保持テスト", changed.getNote());
		assertEquals("UNCONFIRMED", changed.getProgressStatus());
		assertStockQuantity(warehouse, item, "-3");

		// 出荷削除後も元出荷指示書と在庫を保持
		assertEquals(1, service.remove(selectedIds));
		assertNull(service.get(shipmentId));
		assertNotNull(shipInstructionService.get(instruction.getShipInstructionId()));
		assertStockQuantity(warehouse, item, "-3");

		// 削除済み出荷は再削除や状態変更ができない
		assertEquals(0, service.remove(selectedIds));
		assertEquals(0, service.changeStatus(selectedIds, "CONFIRMED"));
		assertStockQuantity(warehouse, item, "-3");
	}

	// 元出荷指示との紐付けと出荷更新・削除後の未出荷残数を確認
	@Test
	public void testInstructionLinkAndRemainingQuantity() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 元出荷指示2個を保存
		ShipInstructionVO instruction = saveInstruction(item, warehouse);

		assertNotNull(instruction);
		assertEquals(1, instruction.getLines().size());

		Long instructionId = instruction.getShipInstructionId();
		Long instructionLineId = instruction.getLines().get(0).getShipInstructionLineId();

		assertNotNull(instructionId);
		assertNotNull(instructionLineId);
		assertRemainingQuantity(instructionId, "2");
		assertStockQuantity(warehouse, item, "-3");

		// 出荷1個を保存 → 未出荷残数1個
		ShipmentVO shipment = createShipment(item, warehouse, instructionLineId, "1");

		Long shipmentId = service.save(shipment);
		ShipmentVO saved = service.get(shipmentId);

		assertNotNull(saved);
		assertEquals(1, saved.getLines().size());
		assertEquals(instructionLineId, saved.getLines().get(0).getShipInstructionLineId());
		assertRemainingQuantity(instructionId, "1");
		assertStockQuantity(warehouse, item, "-3");

		// 未確認状態でも出荷済み数量は残数計算に含める
		assertEquals(1, service.changeStatus(Collections.singletonList(shipmentId), "UNCONFIRMED"));
		assertRemainingQuantity(instructionId, "1");

		// 同じ出荷行を2個に変更 → 未出荷残数0個
		saved.getLines().get(0).setQuantity(new BigDecimal("2"));
		service.save(saved);

		ShipmentVO changed = service.get(shipmentId);

		assertNotNull(changed);
		assertEquals(1, changed.getLines().size());
		assertEquals(instructionLineId, changed.getLines().get(0).getShipInstructionLineId());
		assertRemainingQuantity(instructionId, "0");
		assertStockQuantity(warehouse, item, "-3");

		// 出荷を削除 → 未出荷残数2個へ復元
		assertEquals(1, service.remove(Collections.singletonList(shipmentId)));
		assertNull(service.get(shipmentId));
		assertRemainingQuantity(instructionId, "2");
		assertStockQuantity(warehouse, item, "-3");
	}

	// 元伝票選択で使用する未出荷残数を確認
	private void assertRemainingQuantity(Long instructionId, String expectedQuantity) {

		List<ShipInstructionLookupLineVO> lines = shipInstructionLookupMapper.getLines(instructionId);

		assertEquals(1, lines.size());
		assertEquals(0, new BigDecimal(expectedQuantity).compareTo(lines.get(0).getRemainingQuantity()));
	}

	// テスト出荷伝票を生成
	private ShipmentVO createShipment(ItemVO item, WarehouseVO warehouse, Long instructionLineId, String quantity) {

		ShipmentVO shipment = new ShipmentVO();
		shipment.setBusinessDate(LocalDate.now().toString());
		shipment.setWarehouseId(warehouse.getWarehouseId());
		shipment.setNote("出荷Serviceテスト");

		ShipmentLineVO line = new ShipmentLineVO();
		line.setItemId(item.getItemId());
		line.setShipInstructionLineId(instructionLineId);
		line.setItemName(item.getItemName());
		line.setSpecification(item.getSpecification());
		line.setUnit(item.getUnit());
		line.setQuantity(new BigDecimal(quantity));

		shipment.getLines().add(line);

		return shipment;
	}

	// 元売上3個と元出荷指示2個を保存
	private ShipInstructionVO saveInstruction(ItemVO item, WarehouseVO warehouse) {

		SaleVO sale = new SaleVO();
		sale.setBusinessDate(LocalDate.now().toString());
		sale.setWarehouseId(warehouse.getWarehouseId());
		sale.setNote("出荷の元売上テスト");

		SaleLineVO saleLine = new SaleLineVO();
		saleLine.setItemId(item.getItemId());
		saleLine.setItemName(item.getItemName());
		saleLine.setSpecification(item.getSpecification());
		saleLine.setUnit(item.getUnit());
		saleLine.setQuantity(new BigDecimal("3"));
		saleLine.setUnitPrice(new BigDecimal("1000"));

		sale.getLines().add(saleLine);
		saleService.save(sale);

		assertNotNull(saleLine.getSaleLineId());

		ShipInstructionVO instruction = new ShipInstructionVO();
		instruction.setBusinessDate(LocalDate.now().toString());
		instruction.setWarehouseId(warehouse.getWarehouseId());
		instruction.setNote("出荷の元出荷指示テスト");

		ShipInstructionLineVO instructionLine = new ShipInstructionLineVO();

		instructionLine.setItemId(item.getItemId());
		instructionLine.setSaleLineId(saleLine.getSaleLineId());
		instructionLine.setItemName(item.getItemName());
		instructionLine.setSpecification(item.getSpecification());
		instructionLine.setUnit(item.getUnit());
		instructionLine.setQuantity(new BigDecimal("2"));

		instruction.getLines().add(instructionLine);

		Long instructionId = shipInstructionService.save(instruction);

		assertNotNull(instructionId);

		return shipInstructionService.get(instructionId);
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
		item.setItemCode("SHTEST" + token);
		item.setItemName("出荷テスト品目 " + token);
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
		warehouse.setWarehouseCode("SHWTEST" + token);
		warehouse.setWarehouseName("出荷テスト倉庫 " + token);
		warehouse.setWarehouseType("倉庫");

		return warehouse;
	}

	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}