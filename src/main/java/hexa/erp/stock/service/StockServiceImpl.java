package hexa.erp.stock.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hexa.erp.stock.domain.StockMovementVO;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.stock.mapper.StockMapper;
import hexa.erp.warehouse.domain.WarehouseLookupVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Service
@Log4j
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

	// 在庫Mapperの参照
	private final StockMapper mapper;

	// 現在庫の全件取得
	@Override
	public List<StockVO> getList() {
		return mapper.getList();
	}

	// 利用中の倉庫を取得する。
	@Override
	public List<WarehouseLookupVO> getWarehouseList() {
		return mapper.getWarehouseList();
	}

	// 売上による在庫増減
	@Override
	@Transactional
	public void applySaleMovement(StockMovementVO movement) {

		if (movement == null || movement.getSaleId() == null || movement.getSaleLineId() == null
				|| movement.getWarehouseId() == null || movement.getItemId() == null
				|| movement.getQuantityDelta() == null) {
			throw new IllegalArgumentException("売上、品目行、倉庫、在庫増減数量を入力してください。");
		}

		BigDecimal quantityDelta = movement.getQuantityDelta().setScale(3, RoundingMode.HALF_UP);

		if (quantityDelta.compareTo(BigDecimal.ZERO) == 0) {

			return;
		}

		movement.setQuantityDelta(quantityDelta);

		StockVO current = mapper.read(movement.getWarehouseId(), movement.getItemId());

		if (current == null) {
			current = new StockVO();
			current.setWarehouseId(movement.getWarehouseId());
			current.setItemId(movement.getItemId());
			current.setQuantity(BigDecimal.ZERO);

			mapper.insert(current);
		}

		mapper.addQuantity(movement);
		mapper.insertMovement(movement);

		log.info("売上による在庫増減: 倉庫 " + movement.getWarehouseId() + "、品目 " + movement.getItemId() + "、増減数量 "
				+ quantityDelta);
	}

	// 現在庫と在庫変動履歴を保存する。
	@Override
	@Transactional
	public void adjust(StockVO stock) {
		if (stock == null || stock.getWarehouseId() == null || stock.getItemId() == null
				|| stock.getQuantity() == null) {

			throw new IllegalArgumentException("倉庫、品目、調整後の在庫数量を入力してください。");
		}

		BigDecimal targetQuantity = stock.getQuantity().setScale(3, RoundingMode.HALF_UP);

		StockVO current = mapper.read(stock.getWarehouseId(), stock.getItemId());

		if (current == null) {
			current = new StockVO();
			current.setWarehouseId(stock.getWarehouseId());
			current.setItemId(stock.getItemId());
			current.setQuantity(BigDecimal.ZERO);

			mapper.insert(current);
		}

		BigDecimal quantityDelta = targetQuantity.subtract(current.getQuantity());

		stock.setQuantity(targetQuantity);
		mapper.update(stock);

		if (quantityDelta.compareTo(BigDecimal.ZERO) != 0) {
			StockMovementVO movement = new StockMovementVO();

			movement.setWarehouseId(stock.getWarehouseId());
			movement.setItemId(stock.getItemId());
			movement.setQuantityDelta(quantityDelta);

			mapper.insertMovement(movement);
		}

		log.info("在庫調整: 倉庫 " + stock.getWarehouseId() + "、品目 " + stock.getItemId() + "、調整後数量 " + targetQuantity);
	}
}
