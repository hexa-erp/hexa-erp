package hexa.erp.warehouse.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

import hexa.erp.warehouse.domain.WarehouseCriteria;
import hexa.erp.warehouse.domain.WarehouseVO;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
@Log4j
public class WarehouseServiceTests {

	@Autowired
	private WarehouseService service;

	// Serviceオブジェクトの注入を確認
	@Test
	public void testServiceExist() {
		log.info("========== SERVICE INJECTION TEST ==========");
		log.info("SERVICE: " + service);

		assertNotNull(service);
	}

	// 倉庫の登録と取得を確認
	@Test
	public void testRegisterAndGetList() {
		WarehouseVO warehouse = createWarehouse();

		log.info("========== SERVICE REGISTER TEST ==========");
		log.info("REGISTER BEFORE: " + warehouse);

		service.register(warehouse);

		log.info("GENERATED WAREHOUSE ID: " + warehouse.getWarehouseId());
		log.info("REGISTER AFTER: " + warehouse);

		WarehouseCriteria criteria = createCriteria(warehouse.getWarehouseCode());

		List<WarehouseVO> list = service.getList(criteria);
		int totalCount = service.getTotal(criteria);

		log.info("========== SERVICE LIST RESULT ==========");
		log.info("LIST SIZE: " + list.size());
		log.info("TOTAL COUNT: " + totalCount);
		list.forEach(row -> log.info("WAREHOUSE ROW: " + row));

		WarehouseVO savedWarehouse = findById(list, warehouse.getWarehouseId());

		assertNotNull(warehouse.getWarehouseId());
		assertNotNull(savedWarehouse);
		assertFalse(list.isEmpty());
		assertEquals(1, totalCount);
	}

	// 倉庫情報の更新を確認
	@Test
	public void testModify() {
		WarehouseVO warehouse = createWarehouse();
		service.register(warehouse);

		String changedName = "Service更新テスト倉庫 " + createToken();

		warehouse.setWarehouseName(changedName);
		warehouse.setWarehouseType("工場");

		log.info("========== SERVICE MODIFY TEST ==========");
		log.info("WAREHOUSE ID: " + warehouse.getWarehouseId());
		log.info("CHANGED WAREHOUSE NAME: " + changedName);
		log.info("CHANGED WAREHOUSE TYPE: 工場");

		boolean result = service.modify(warehouse);

		WarehouseCriteria criteria = createCriteria(changedName);

		WarehouseVO changedWarehouse = findById(service.getList(criteria), warehouse.getWarehouseId());

		log.info("MODIFY RESULT: " + result);
		log.info("UPDATED WAREHOUSE: " + changedWarehouse);

		assertTrue(result);
		assertNotNull(changedWarehouse);
		assertEquals(changedName, changedWarehouse.getWarehouseName());
		assertEquals("工場", changedWarehouse.getWarehouseType());
	}

	// 倉庫の利用停止と利用再開を確認
	@Test
	public void testChangeActive() {
		WarehouseVO warehouse = createWarehouse();
		service.register(warehouse);

		log.info("========== SERVICE STOP WAREHOUSE TEST ==========");
		log.info("WAREHOUSE ID: " + warehouse.getWarehouseId());

		int stopCount = service.changeActive(Collections.singletonList(warehouse.getWarehouseId()), "N");

		WarehouseCriteria criteria = createCriteria(warehouse.getWarehouseCode());

		WarehouseVO stoppedWarehouse = findById(service.getList(criteria), warehouse.getWarehouseId());

		log.info("STOP COUNT: " + stopCount);
		log.info("STOPPED ACTIVE FLAG: " + stoppedWarehouse.getActiveFlag());

		assertEquals(1, stopCount);
		assertNotNull(stoppedWarehouse);
		assertEquals("N", stoppedWarehouse.getActiveFlag());

		log.info("========== SERVICE REUSE WAREHOUSE TEST ==========");

		int reuseCount = service.changeActive(Collections.singletonList(warehouse.getWarehouseId()), "Y");

		WarehouseVO reusedWarehouse = findById(service.getList(criteria), warehouse.getWarehouseId());

		log.info("REUSE COUNT: " + reuseCount);
		log.info("REUSED ACTIVE FLAG: " + reusedWarehouse.getActiveFlag());

		assertEquals(1, reuseCount);
		assertNotNull(reusedWarehouse);
		assertEquals("Y", reusedWarehouse.getActiveFlag());
	}

	private WarehouseCriteria createCriteria(String keyword) {
		WarehouseCriteria criteria = new WarehouseCriteria();

		criteria.setPage(1);
		criteria.setPageSize(50);
		criteria.setKeyword(keyword);
		criteria.setIncludeInactive("Y");

		return criteria;
	}

	private WarehouseVO findById(List<WarehouseVO> list, Long warehouseId) {

		return list.stream().filter(warehouse -> warehouseId.equals(warehouse.getWarehouseId())).findFirst()
				.orElse(null);
	}

	private WarehouseVO createWarehouse() {
		String token = createToken();

		WarehouseVO warehouse = new WarehouseVO();
		warehouse.setWarehouseCode("TEST" + token);
		warehouse.setWarehouseName("Serviceテスト倉庫 " + token);
		warehouse.setWarehouseType("倉庫");

		return warehouse;
	}

	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}
