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

	// 출하지시 저장·수정과 원판매 연결, 재고 유지 확인
	@Test
	public void testSaveAndUpdateWithoutChangingStock() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 판매 3개 저장: 이 시점에만 재고가 차감
		SaleVO sale = createSale(item, warehouse);
		Long saleId = saleService.save(sale);

		assertNotNull(saleId);

		Long saleLineId = sale.getLines().get(0).getSaleLineId();

		assertNotNull(saleLineId);
		assertStockQuantity(warehouse, item, "-3");

		// 원판매 상세행을 연결해 출하지시 2개 저장
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

		// 같은 출하지시 상세행의 수량을 1개로 수정
		ShipInstructionVO update = createInstruction(item, warehouse, saleLineId, "1");

		update.setShipInstructionId(instructionId);
		update.setNote("출하지시 수정 테스트");
		update.getLines().get(0).setShipInstructionLineId(lineId);

		Long updatedId = service.save(update);
		ShipInstructionVO changed = service.get(instructionId);

		assertEquals(instructionId, updatedId);
		assertNotNull(changed);
		assertEquals(saved.getShipInstructionNo(), changed.getShipInstructionNo());
		assertEquals("출하지시 수정 테스트", changed.getNote());
		assertEquals(1, changed.getLines().size());

		ShipInstructionLineVO changedLine = changed.getLines().get(0);

		assertEquals(lineId, changedLine.getShipInstructionLineId());
		assertEquals(saleLineId, changedLine.getSaleLineId());
		assertEquals(0, new BigDecimal("1").compareTo(changedLine.getQuantity()));
		assertEquals(0, new BigDecimal("1").compareTo(changed.getTotalQuantity()));
		assertStockQuantity(warehouse, item, "-3");
	}

	// 출하지시 상태 변경·삭제와 재고 유지 확인
	@Test
	public void testChangeStatusAndRemoveWithoutChangingStock() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 원판매 저장: 재고 -3
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

		// 진행상태를 완료로 변경
		int changeCount = service.changeStatus(selectedIds, "COMPLETED");
		ShipInstructionVO completed = service.get(instructionId);

		assertEquals(1, changeCount);
		assertNotNull(completed);
		assertEquals("COMPLETED", completed.getProgressStatus());
		assertEquals(1, completed.getLines().size());
		assertStockQuantity(warehouse, item, "-3");

		// 일반 수정으로 완료 상태가 초기화되지 않는지 확인
		ShipInstructionVO update = createInstruction(item, warehouse, saleLineId, "2");

		update.setShipInstructionId(instructionId);
		update.setProgressStatus("IN_PROGRESS");
		update.setNote("출하지시 완료 상태 유지 테스트");
		update.getLines().get(0).setShipInstructionLineId(completed.getLines().get(0).getShipInstructionLineId());

		service.save(update);

		ShipInstructionVO changed = service.get(instructionId);

		assertNotNull(changed);
		assertEquals("출하지시 완료 상태 유지 테스트", changed.getNote());
		assertEquals("COMPLETED", changed.getProgressStatus());
		assertStockQuantity(warehouse, item, "-3");

		// 지시서 삭제 후에도 원판매와 재고는 유지
		assertEquals(1, service.remove(selectedIds));
		assertNull(service.get(instructionId));
		assertNotNull(saleService.get(sale.getSaleId()));
		assertStockQuantity(warehouse, item, "-3");

		// 이미 삭제된 지시서는 다시 삭제하거나 상태를 변경할 수 없음
		assertEquals(0, service.remove(selectedIds));
		assertEquals(0, service.changeStatus(selectedIds, "IN_PROGRESS"));
		assertStockQuantity(warehouse, item, "-3");
	}

	// 원판매 연결과 출하지시 수정·삭제에 따른 미지시 잔량 확인
	@Test
	public void testSaleLinkAndRemainingQuantity() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		WarehouseVO warehouse = createWarehouse();
		warehouseMapper.insertSelectKey(warehouse);

		// 원판매 3개 저장
		SaleVO sale = createSale(item, warehouse);
		Long saleId = saleService.save(sale);
		Long saleLineId = sale.getLines().get(0).getSaleLineId();

		assertNotNull(saleId);
		assertNotNull(saleLineId);
		assertRemainingQuantity(saleId, "3");
		assertStockQuantity(warehouse, item, "-3");

		// 출하지시 2개 저장 → 미지시 잔량 1개
		ShipInstructionVO instruction = createInstruction(item, warehouse, saleLineId, "2");

		Long instructionId = service.save(instruction);
		ShipInstructionVO saved = service.get(instructionId);

		assertNotNull(saved);
		assertEquals(1, saved.getLines().size());
		assertEquals(saleLineId, saved.getLines().get(0).getSaleLineId());
		assertRemainingQuantity(saleId, "1");
		assertStockQuantity(warehouse, item, "-3");

		// 완료 상태여도 지시한 수량은 잔량 계산에 포함
		assertEquals(1, service.changeStatus(Collections.singletonList(instructionId), "COMPLETED"));
		assertRemainingQuantity(saleId, "1");

		// 같은 지시 행의 수량을 1개로 수정 → 미지시 잔량 2개
		saved.getLines().get(0).setQuantity(new BigDecimal("1"));
		service.save(saved);

		ShipInstructionVO changed = service.get(instructionId);

		assertNotNull(changed);
		assertEquals(1, changed.getLines().size());
		assertEquals(saleLineId, changed.getLines().get(0).getSaleLineId());
		assertRemainingQuantity(saleId, "2");
		assertStockQuantity(warehouse, item, "-3");

		// 지시서 삭제 → 미지시 잔량 3개로 복원
		assertEquals(1, service.remove(Collections.singletonList(instructionId)));
		assertNull(service.get(instructionId));
		assertRemainingQuantity(saleId, "3");
		assertStockQuantity(warehouse, item, "-3");
	}

	// 원본 전표 선택 조회에서 사용하는 미지시 잔량 확인
	private void assertRemainingQuantity(Long saleId, String expectedQuantity) {

		List<SaleLookupLineVO> lines = saleLookupMapper.getLines(saleId);

		assertEquals(1, lines.size());
		assertEquals(0, new BigDecimal(expectedQuantity).compareTo(lines.get(0).getRemainingQuantity()));
	}

	// 테스트 출하지시서 생성
	private ShipInstructionVO createInstruction(ItemVO item, WarehouseVO warehouse, Long saleLineId, String quantity) {

		ShipInstructionVO instruction = new ShipInstructionVO();
		instruction.setBusinessDate(LocalDate.now().toString());
		instruction.setWarehouseId(warehouse.getWarehouseId());
		instruction.setNote("출하지시 Service 테스트");

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

	// 원본으로 사용할 테스트 판매 생성
	private SaleVO createSale(ItemVO item, WarehouseVO warehouse) {
		SaleVO sale = new SaleVO();
		sale.setBusinessDate(LocalDate.now().toString());
		sale.setWarehouseId(warehouse.getWarehouseId());
		sale.setNote("출하지시 원판매 테스트");

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
		item.setItemCode("SITEST" + token);
		item.setItemName("출하지시 테스트 품목 " + token);
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
		warehouse.setWarehouseCode("SIWTEST" + token);
		warehouse.setWarehouseName("출하지시 테스트 창고 " + token);
		warehouse.setWarehouseType("창고");

		return warehouse;
	}

	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}