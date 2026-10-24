package hexa.erp.testinfra;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import hexa.erp.partner.domain.PartnerCriteria;
import hexa.erp.partner.domain.PartnerVO;
import hexa.erp.warehouse.domain.WarehouseCriteria;
import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import hexa.erp.item.domain.ItemCriteria;
import hexa.erp.item.domain.ItemVO;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.stock.domain.StockMovementVO;
import hexa.erp.quotation.domain.QuotationCriteria;
import hexa.erp.quotation.domain.QuotationVO;
import hexa.erp.quotation.domain.QuotationLineVO;
import hexa.erp.quotation.domain.QuotationReportCriteria;
import hexa.erp.quotation.domain.QuotationReportVO;
import hexa.erp.salesorder.domain.SalesOrderCriteria;
import hexa.erp.salesorder.domain.SalesOrderVO;
import hexa.erp.salesorder.domain.SalesOrderLineVO;
import hexa.erp.salesorder.domain.SalesOrderReportCriteria;
import hexa.erp.salesorder.domain.SalesOrderReportVO;
import hexa.erp.sale.domain.SaleCriteria;
import hexa.erp.sale.domain.SaleVO;
import hexa.erp.sale.domain.SaleLineVO;
import hexa.erp.sale.domain.SalePriceCriteria;
import hexa.erp.sale.domain.SalePriceVO;
import hexa.erp.sale.domain.SalePriceChangeVO;
import hexa.erp.sale.domain.SaleStatusCriteria;
import hexa.erp.sale.domain.SaleStatusVO;
import hexa.erp.shipinstruction.domain.ShipInstructionCriteria;
import hexa.erp.shipinstruction.domain.ShipInstructionVO;
import hexa.erp.shipinstruction.domain.ShipInstructionLineVO;
import hexa.erp.shipment.domain.ShipmentCriteria;
import hexa.erp.shipment.domain.ShipmentVO;
import hexa.erp.shipment.domain.ShipmentLineVO;
import hexa.erp.partner.service.PartnerService;
import hexa.erp.warehouse.service.WarehouseService;
import hexa.erp.item.service.ItemService;
import hexa.erp.item.service.ItemImageService;
import hexa.erp.stock.service.StockService;
import hexa.erp.quotation.service.QuotationService;
import hexa.erp.quotation.service.QuotationReportService;
import hexa.erp.salesorder.service.SalesOrderService;
import hexa.erp.salesorder.service.SalesOrderReportService;
import hexa.erp.sale.service.SaleService;
import hexa.erp.sale.service.SaleStatusService;
import hexa.erp.sale.service.SalePriceService;
import hexa.erp.shipinstruction.service.ShipInstructionService;
import hexa.erp.shipment.service.ShipmentService;

/** 웹 화면 조회용 고정 데이터. DB 연결과 파일 접근, 업무 데이터 변경은 허용하지 않는다. */
public final class StubBusinessServices {

	private static final Long ID = 1L;
	private static final String CODE = "00001";
	private static final String DATE = "2026-01-01";
	private static final String PARTNER_NAME = "웹 테스트 거래처";
	private static final String WAREHOUSE_NAME = "웹 테스트 창고";
	private static final String ITEM_NAME = "웹 테스트 품목";
	private static final String ASSIGNEE_NAME = "웹 테스트 담당자";
	private static final BigDecimal UNIT_PRICE = new BigDecimal("100");
	private static final BigDecimal VAT_AMOUNT = new BigDecimal("10");
	private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("110");

	private StubBusinessServices() {
	}

	private static AssertionError unsupported(String operation) {
		return new AssertionError("웹 조회 테스트에서 허용하지 않는 작업: " + operation);
	}

	private static PartnerVO partner() {
		PartnerVO row = new PartnerVO();
		row.setPartnerId(ID);
		row.setPartnerCode(CODE);
		row.setPartnerName(PARTNER_NAME);
		row.setActiveFlag("Y");
		return row;
	}

	private static WarehouseVO warehouse() {
		WarehouseVO row = new WarehouseVO();
		row.setWarehouseId(ID);
		row.setWarehouseCode(CODE);
		row.setWarehouseName(WAREHOUSE_NAME);
		row.setWarehouseType("창고");
		row.setActiveFlag("Y");
		return row;
	}

	private static ItemVO item() {
		ItemVO row = new ItemVO();
		row.setItemId(ID);
		row.setItemCode(CODE);
		row.setItemName(ITEM_NAME);
		row.setSpecification("선택 테스트 규격");
		row.setUnit("EA");
		row.setItemType("상품");
		row.setInboundPrice(new BigDecimal("80"));
		row.setOutboundPrice(UNIT_PRICE);
		row.setActiveFlag("Y");
		return row;
	}

	private static QuotationVO quotation() {
		QuotationVO row = new QuotationVO();
		row.setQuotationId(ID);
		row.setQuotationNo(CODE);
		row.setBusinessDate(DATE);
		row.setPartnerName(PARTNER_NAME);
		row.setWarehouseName(WAREHOUSE_NAME);
		row.setAssigneeName(ASSIGNEE_NAME);
		row.setDeletedYn("N");
		row.setItemSummary(ITEM_NAME);
		row.setTotalAmount(TOTAL_AMOUNT);
		QuotationLineVO line = new QuotationLineVO();
		line.setQuotationLineId(ID);
		line.setQuotationId(ID);
		line.setItemId(ID);
		line.setItemCode(CODE);
		line.setItemName(ITEM_NAME);
		line.setUnit("EA");
		line.setQuantity(BigDecimal.ONE);
		line.setDeletedYn("N");
		line.setUnitPrice(UNIT_PRICE);
		line.setSupplyAmount(UNIT_PRICE);
		line.setVatAmount(VAT_AMOUNT);
		line.setTotalAmount(TOTAL_AMOUNT);
		row.getLines().add(line);
		return row;
	}

	private static SalesOrderVO salesOrder() {
		SalesOrderVO row = new SalesOrderVO();
		row.setSalesOrderId(ID);
		row.setSalesOrderNo(CODE);
		row.setBusinessDate(DATE);
		row.setPartnerName(PARTNER_NAME);
		row.setWarehouseName(WAREHOUSE_NAME);
		row.setAssigneeName(ASSIGNEE_NAME);
		row.setDeletedYn("N");
		row.setItemSummary(ITEM_NAME);
		row.setTotalAmount(TOTAL_AMOUNT);
		SalesOrderLineVO line = new SalesOrderLineVO();
		line.setSalesOrderLineId(ID);
		line.setSalesOrderId(ID);
		line.setItemId(ID);
		line.setItemCode(CODE);
		line.setItemName(ITEM_NAME);
		line.setUnit("EA");
		line.setQuantity(BigDecimal.ONE);
		line.setDeletedYn("N");
		line.setQuotationLineId(ID);
		line.setUnitPrice(UNIT_PRICE);
		line.setSupplyAmount(UNIT_PRICE);
		line.setVatAmount(VAT_AMOUNT);
		line.setTotalAmount(TOTAL_AMOUNT);
		row.getLines().add(line);
		return row;
	}

	private static SaleVO sale() {
		SaleVO row = new SaleVO();
		row.setSaleId(ID);
		row.setSaleNo(CODE);
		row.setBusinessDate(DATE);
		row.setPartnerName(PARTNER_NAME);
		row.setWarehouseName(WAREHOUSE_NAME);
		row.setAssigneeName(ASSIGNEE_NAME);
		row.setDeletedYn("N");
		row.setItemSummary(ITEM_NAME);
		row.setTotalAmount(TOTAL_AMOUNT);
		SaleLineVO line = new SaleLineVO();
		line.setSaleLineId(ID);
		line.setSaleId(ID);
		line.setItemId(ID);
		line.setItemCode(CODE);
		line.setItemName(ITEM_NAME);
		line.setUnit("EA");
		line.setQuantity(BigDecimal.ONE);
		line.setDeletedYn("N");
		line.setSalesOrderLineId(ID);
		line.setUnitPrice(UNIT_PRICE);
		line.setSupplyAmount(UNIT_PRICE);
		line.setVatAmount(VAT_AMOUNT);
		line.setTotalAmount(TOTAL_AMOUNT);
		row.getLines().add(line);
		return row;
	}

	private static ShipInstructionVO shipInstruction() {
		ShipInstructionVO row = new ShipInstructionVO();
		row.setShipInstructionId(ID);
		row.setShipInstructionNo(CODE);
		row.setBusinessDate(DATE);
		row.setPartnerName(PARTNER_NAME);
		row.setWarehouseName(WAREHOUSE_NAME);
		row.setAssigneeName(ASSIGNEE_NAME);
		row.setDeletedYn("N");
		row.setItemSummary(ITEM_NAME);
		row.setTotalQuantity(BigDecimal.ONE);
		ShipInstructionLineVO line = new ShipInstructionLineVO();
		line.setShipInstructionLineId(ID);
		line.setShipInstructionId(ID);
		line.setItemId(ID);
		line.setItemCode(CODE);
		line.setItemName(ITEM_NAME);
		line.setUnit("EA");
		line.setQuantity(BigDecimal.ONE);
		line.setDeletedYn("N");
		line.setSaleLineId(ID);
		row.getLines().add(line);
		return row;
	}

	private static ShipmentVO shipment() {
		ShipmentVO row = new ShipmentVO();
		row.setShipmentId(ID);
		row.setShipmentNo(CODE);
		row.setBusinessDate(DATE);
		row.setPartnerName(PARTNER_NAME);
		row.setWarehouseName(WAREHOUSE_NAME);
		row.setAssigneeName(ASSIGNEE_NAME);
		row.setDeletedYn("N");
		row.setItemSummary(ITEM_NAME);
		row.setTotalQuantity(BigDecimal.ONE);
		ShipmentLineVO line = new ShipmentLineVO();
		line.setShipmentLineId(ID);
		line.setShipmentId(ID);
		line.setItemId(ID);
		line.setItemCode(CODE);
		line.setItemName(ITEM_NAME);
		line.setUnit("EA");
		line.setQuantity(BigDecimal.ONE);
		line.setDeletedYn("N");
		line.setShipInstructionLineId(ID);
		row.getLines().add(line);
		return row;
	}

	private static SalePriceVO salePrice() {
		SalePriceVO row = new SalePriceVO();
		row.setSaleId(ID);
		row.setSaleLineId(ID);
		row.setBusinessDate(DATE);
		row.setSaleNo(CODE);
		row.setPartnerName(PARTNER_NAME);
		row.setAssigneeName(ASSIGNEE_NAME);
		row.setWarehouseName(WAREHOUSE_NAME);
		row.setItemCode(CODE);
		row.setItemName(ITEM_NAME);
		row.setQuantity(BigDecimal.ONE);
		row.setUnit("EA");
		row.setUnitPrice(UNIT_PRICE);
		row.setSupplyAmount(UNIT_PRICE);
		row.setVatAmount(VAT_AMOUNT);
		row.setTotalAmount(TOTAL_AMOUNT);
		return row;
	}

	public static final class Partner implements PartnerService {

		@Override
		public List<PartnerVO> getList(PartnerCriteria criteria) {
			return Collections.singletonList(partner());
		}

		@Override
		public int getTotal(PartnerCriteria criteria) {
			return 1;
		}

		@Override
		public void register(PartnerVO form) {
			throw unsupported("PartnerService.register");
		}

		@Override
		public boolean modify(PartnerVO form) {
			throw unsupported("PartnerService.modify");
		}

		@Override
		public int changeActive(List<Long> ids, String activeFlag) {
			throw unsupported("PartnerService.changeActive");
		}

	}

	public static final class Warehouse implements WarehouseService {

		@Override
		public List<WarehouseVO> getList(WarehouseCriteria criteria) {
			return Collections.singletonList(warehouse());
		}

		@Override
		public int getTotal(WarehouseCriteria criteria) {
			return 1;
		}

		@Override
		public void register(WarehouseVO form) {
			throw unsupported("WarehouseService.register");
		}

		@Override
		public boolean modify(WarehouseVO form) {
			throw unsupported("WarehouseService.modify");
		}

		@Override
		public int changeActive(List<Long> ids, String activeFlag) {
			throw unsupported("WarehouseService.changeActive");
		}

	}

	public static final class Item implements ItemService {

		@Override
		public List<ItemVO> getList(ItemCriteria criteria) {
			return Collections.singletonList(item());
		}

		@Override
		public int getTotal(ItemCriteria criteria) {
			return 1;
		}

		@Override
		public ItemVO get(Long id) {
			return ID.equals(id) ? item() : null;
		}

		@Override
		public void register(ItemVO form) {
			throw unsupported("ItemService.register");
		}

		@Override
		public boolean modify(ItemVO form) {
			throw unsupported("ItemService.modify");
		}

		@Override
		public int changeActive(List<Long> ids, String activeFlag) {
			throw unsupported("ItemService.changeActive");
		}

	}

	public static final class ItemImage implements ItemImageService {

		@Override
		public String store(MultipartFile imageFile) {
			throw unsupported("ItemImageService.store");
		}

		@Override
		public File getFile(String imagePath, boolean thumbnail) {
			throw unsupported("ItemImageService.getFile");
		}

		@Override
		public void delete(String imagePath) {
			throw unsupported("ItemImageService.delete");
		}

	}

	public static final class Stock implements StockService {

		@Override
		public List<StockVO> getList() {
			return Collections.emptyList();
		}

		@Override
		public List<WarehouseLookupVO> getWarehouseList() {
			return Collections.emptyList();
		}

		@Override
		public void adjust(StockVO stock) {
			throw unsupported("StockService.adjust");
		}

		@Override
		public void applySaleMovement(StockMovementVO movement) {
			throw unsupported("StockService.applySaleMovement");
		}

	}

	public static final class Quotation implements QuotationService {

		@Override
		public List<QuotationVO> getList(QuotationCriteria criteria) {
			return Collections.singletonList(quotation());
		}

		@Override
		public int getTotal(QuotationCriteria criteria) {
			return 1;
		}

		@Override
		public QuotationVO get(Long id) {
			return ID.equals(id) ? quotation() : null;
		}

		@Override
		public Long save(QuotationVO form) {
			throw unsupported("QuotationService.save");
		}

		@Override
		public int remove(List<Long> ids) {
			throw unsupported("QuotationService.remove");
		}

		@Override
		public int changeStatus(List<Long> ids, String progressStatus) {
			throw unsupported("QuotationService.changeStatus");
		}

	}

	public static final class QuotationReport implements QuotationReportService {

		@Override
		public QuotationReportVO getStatus(QuotationReportCriteria criteria) {
			return new QuotationReportVO();
		}

		@Override
		public QuotationReportVO getUnordered(QuotationReportCriteria criteria) {
			return new QuotationReportVO();
		}

	}

	public static final class SalesOrder implements SalesOrderService {

		@Override
		public List<SalesOrderVO> getList(SalesOrderCriteria criteria) {
			return Collections.singletonList(salesOrder());
		}

		@Override
		public int getTotal(SalesOrderCriteria criteria) {
			return 1;
		}

		@Override
		public SalesOrderVO get(Long id) {
			return ID.equals(id) ? salesOrder() : null;
		}

		@Override
		public Long save(SalesOrderVO form) {
			throw unsupported("SalesOrderService.save");
		}

		@Override
		public int remove(List<Long> ids) {
			throw unsupported("SalesOrderService.remove");
		}

		@Override
		public int changeStatus(List<Long> ids, String progressStatus) {
			throw unsupported("SalesOrderService.changeStatus");
		}

	}

	public static final class SalesOrderReport implements SalesOrderReportService {

		@Override
		public SalesOrderReportVO getStatus(SalesOrderReportCriteria criteria) {
			return new SalesOrderReportVO();
		}

		@Override
		public SalesOrderReportVO getUnsold(SalesOrderReportCriteria criteria) {
			return new SalesOrderReportVO();
		}

	}

	public static final class Sale implements SaleService {

		@Override
		public List<SaleVO> getList(SaleCriteria criteria) {
			return Collections.singletonList(sale());
		}

		@Override
		public int getTotal(SaleCriteria criteria) {
			return 1;
		}

		@Override
		public SaleVO get(Long id) {
			return ID.equals(id) ? sale() : null;
		}

		@Override
		public Long save(SaleVO form) {
			throw unsupported("SaleService.save");
		}

		@Override
		public int remove(List<Long> ids) {
			throw unsupported("SaleService.remove");
		}

		@Override
		public int changeStatus(List<Long> ids, String progressStatus) {
			throw unsupported("SaleService.changeStatus");
		}

	}

	public static final class SaleStatus implements SaleStatusService {

		@Override
		public SaleStatusVO getStatus(SaleStatusCriteria criteria) {
			return new SaleStatusVO();
		}

	}

	public static final class SalePrice implements SalePriceService {

		@Override
		public List<SalePriceVO> getList(SalePriceCriteria criteria) {
			return Collections.singletonList(salePrice());
		}

		@Override
		public int getTotal(SalePriceCriteria criteria) {
			return 1;
		}

		@Override
		public int savePrices(List<SalePriceChangeVO> changes) {
			throw unsupported("SalePriceService.savePrices");
		}

	}

	public static final class ShipInstruction implements ShipInstructionService {

		@Override
		public List<ShipInstructionVO> getList(ShipInstructionCriteria criteria) {
			return Collections.singletonList(shipInstruction());
		}

		@Override
		public int getTotal(ShipInstructionCriteria criteria) {
			return 1;
		}

		@Override
		public ShipInstructionVO get(Long id) {
			return ID.equals(id) ? shipInstruction() : null;
		}

		@Override
		public Long save(ShipInstructionVO form) {
			throw unsupported("ShipInstructionService.save");
		}

		@Override
		public int remove(List<Long> ids) {
			throw unsupported("ShipInstructionService.remove");
		}

		@Override
		public int changeStatus(List<Long> ids, String progressStatus) {
			throw unsupported("ShipInstructionService.changeStatus");
		}

	}

	public static final class Shipment implements ShipmentService {

		@Override
		public List<ShipmentVO> getList(ShipmentCriteria criteria) {
			return Collections.singletonList(shipment());
		}

		@Override
		public int getTotal(ShipmentCriteria criteria) {
			return 1;
		}

		@Override
		public ShipmentVO get(Long id) {
			return ID.equals(id) ? shipment() : null;
		}

		@Override
		public Long save(ShipmentVO form) {
			throw unsupported("ShipmentService.save");
		}

		@Override
		public int remove(List<Long> ids) {
			throw unsupported("ShipmentService.remove");
		}

		@Override
		public int changeStatus(List<Long> ids, String progressStatus) {
			throw unsupported("ShipmentService.changeStatus");
		}

	}

}
