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

	// 출하 저장·수정과 원출하지시 연결, 재고 유지 확인
	@Test
	public void testSaveAndUpdateWithoutChangingStock() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 원판매 3개와 출하지시 2개 저장
		ShipInstructionVO instruction = saveInstruction(item, warehouse);

		assertNotNull(instruction);
		assertEquals(1, instruction.getLines().size());

		Long instructionLineId = instruction.getLines().get(0).getShipInstructionLineId();

		assertNotNull(instructionLineId);
		assertStockQuantity(warehouse, item, "-3");

		// 원출하지시 상세행을 연결해 출하 1개 저장
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

		// 같은 출하 상세행의 수량을 2개로 수정
		ShipmentVO update = createShipment(item, warehouse, instructionLineId, "2");

		update.setShipmentId(shipmentId);
		update.setNote("출하 수정 테스트");
		update.getLines().get(0).setShipmentLineId(lineId);

		Long updatedId = service.save(update);
		ShipmentVO changed = service.get(shipmentId);

		assertEquals(shipmentId, updatedId);
		assertNotNull(changed);
		assertEquals(saved.getShipmentNo(), changed.getShipmentNo());
		assertEquals("출하 수정 테스트", changed.getNote());
		assertEquals(1, changed.getLines().size());

		ShipmentLineVO changedLine = changed.getLines().get(0);

		assertEquals(lineId, changedLine.getShipmentLineId());
		assertEquals(instructionLineId, changedLine.getShipInstructionLineId());
		assertEquals(0, new BigDecimal("2").compareTo(changedLine.getQuantity()));
		assertEquals(0, new BigDecimal("2").compareTo(changed.getTotalQuantity()));
		assertStockQuantity(warehouse, item, "-3");
	}

	// 출하 상태 변경·삭제와 재고 유지 확인
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

		// 진행상태를 미확인으로 변경
		int changeCount = service.changeStatus(selectedIds, "UNCONFIRMED");
		ShipmentVO unconfirmed = service.get(shipmentId);

		assertEquals(1, changeCount);
		assertNotNull(unconfirmed);
		assertEquals("UNCONFIRMED", unconfirmed.getProgressStatus());
		assertEquals(1, unconfirmed.getLines().size());
		assertStockQuantity(warehouse, item, "-3");

		// 일반 수정으로 미확인 상태가 초기화되지 않는지 확인
		ShipmentVO update = createShipment(item, warehouse, instructionLineId, "1");

		update.setShipmentId(shipmentId);
		update.setProgressStatus("CONFIRMED");
		update.setNote("출하 미확인 상태 유지 테스트");
		update.getLines().get(0).setShipmentLineId(unconfirmed.getLines().get(0).getShipmentLineId());

		service.save(update);

		ShipmentVO changed = service.get(shipmentId);

		assertNotNull(changed);
		assertEquals("출하 미확인 상태 유지 테스트", changed.getNote());
		assertEquals("UNCONFIRMED", changed.getProgressStatus());
		assertStockQuantity(warehouse, item, "-3");

		// 출하 삭제 후에도 원출하지시서와 재고는 유지
		assertEquals(1, service.remove(selectedIds));
		assertNull(service.get(shipmentId));
		assertNotNull(shipInstructionService.get(instruction.getShipInstructionId()));
		assertStockQuantity(warehouse, item, "-3");

		// 이미 삭제된 출하는 다시 삭제하거나 상태를 변경할 수 없음
		assertEquals(0, service.remove(selectedIds));
		assertEquals(0, service.changeStatus(selectedIds, "CONFIRMED"));
		assertStockQuantity(warehouse, item, "-3");
	}

	// 원출하지시 연결과 출하 수정·삭제에 따른 미출하 잔량 확인
	@Test
	public void testInstructionLinkAndRemainingQuantity() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 원출하지시 2개 저장
		ShipInstructionVO instruction = saveInstruction(item, warehouse);

		assertNotNull(instruction);
		assertEquals(1, instruction.getLines().size());

		Long instructionId = instruction.getShipInstructionId();
		Long instructionLineId = instruction.getLines().get(0).getShipInstructionLineId();

		assertNotNull(instructionId);
		assertNotNull(instructionLineId);
		assertRemainingQuantity(instructionId, "2");
		assertStockQuantity(warehouse, item, "-3");

		// 출하 1개 저장 → 미출하 잔량 1개
		ShipmentVO shipment = createShipment(item, warehouse, instructionLineId, "1");

		Long shipmentId = service.save(shipment);
		ShipmentVO saved = service.get(shipmentId);

		assertNotNull(saved);
		assertEquals(1, saved.getLines().size());
		assertEquals(instructionLineId, saved.getLines().get(0).getShipInstructionLineId());
		assertRemainingQuantity(instructionId, "1");
		assertStockQuantity(warehouse, item, "-3");

		// 미확인 상태여도 출하한 수량은 잔량 계산에 포함
		assertEquals(1, service.changeStatus(Collections.singletonList(shipmentId), "UNCONFIRMED"));
		assertRemainingQuantity(instructionId, "1");

		// 같은 출하 행을 2개로 수정 → 미출하 잔량 0개
		saved.getLines().get(0).setQuantity(new BigDecimal("2"));
		service.save(saved);

		ShipmentVO changed = service.get(shipmentId);

		assertNotNull(changed);
		assertEquals(1, changed.getLines().size());
		assertEquals(instructionLineId, changed.getLines().get(0).getShipInstructionLineId());
		assertRemainingQuantity(instructionId, "0");
		assertStockQuantity(warehouse, item, "-3");

		// 출하 삭제 → 미출하 잔량 2개로 복원
		assertEquals(1, service.remove(Collections.singletonList(shipmentId)));
		assertNull(service.get(shipmentId));
		assertRemainingQuantity(instructionId, "2");
		assertStockQuantity(warehouse, item, "-3");
	}

	// 원본 전표 선택 조회에서 사용하는 미출하 잔량 확인
	private void assertRemainingQuantity(Long instructionId, String expectedQuantity) {

		List<ShipInstructionLookupLineVO> lines = shipInstructionLookupMapper.getLines(instructionId);

		assertEquals(1, lines.size());
		assertEquals(0, new BigDecimal(expectedQuantity).compareTo(lines.get(0).getRemainingQuantity()));
	}

	// 테스트 출하 전표 생성
	private ShipmentVO createShipment(ItemVO item, WarehouseVO warehouse, Long instructionLineId, String quantity) {

		ShipmentVO shipment = new ShipmentVO();
		shipment.setBusinessDate(LocalDate.now().toString());
		shipment.setWarehouseId(warehouse.getWarehouseId());
		shipment.setNote("출하 Service 테스트");

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

	// 원판매 3개와 원출하지시 2개를 저장
	private ShipInstructionVO saveInstruction(ItemVO item, WarehouseVO warehouse) {

		SaleVO sale = new SaleVO();
		sale.setBusinessDate(LocalDate.now().toString());
		sale.setWarehouseId(warehouse.getWarehouseId());
		sale.setNote("출하 원판매 테스트");

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
		instruction.setNote("출하 원출하지시 테스트");

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

	// 현재 재고 확인
	private void assertStockQuantity(WarehouseVO warehouse, ItemVO item, String expectedQuantity) {

		StockVO stock = stockMapper.read(warehouse.getWarehouseId(), item.getItemId());

		assertNotNull(stock);
		assertEquals(0, new BigDecimal(expectedQuantity).compareTo(stock.getQuantity()));
	}

	// 테스트 품목 생성
	private ItemVO createItem() {
		String token = createToken();

		ItemVO item = new ItemVO();
		item.setItemCode("SHTEST" + token);
		item.setItemName("출하 테스트 품목 " + token);
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
		warehouse.setWarehouseCode("SHWTEST" + token);
		warehouse.setWarehouseName("출하 테스트 창고 " + token);
		warehouse.setWarehouseType("창고");

		return warehouse;
	}

	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}