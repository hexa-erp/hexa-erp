package hexa.erp.item.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import hexa.erp.item.domain.ItemCriteria;
import hexa.erp.item.domain.ItemVO;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
@Log4j
public class ItemServiceTests {

	@Autowired
	private ItemService service;

	// Serviceオブジェクトの注入を確認
	@Test
	public void testServiceExist() {
		log.info("========== SERVICE INJECTION TEST ==========");
		log.info("SERVICE: " + service);

		assertNotNull(service);
	}

	// 品目の登録と取得を確認
	@Test
	public void testRegisterAndGet() {
		ItemVO item = createItem();

		log.info("========== SERVICE REGISTER TEST ==========");
		log.info("REGISTER BEFORE: " + item);

		service.register(item);

		ItemVO savedItem = service.get(item.getItemId());

		log.info("GENERATED ITEM ID: " + item.getItemId());
		log.info("SAVED ITEM: " + savedItem);

		ItemCriteria criteria = createCriteria(item.getItemCode());

		log.info("SEARCH TOTAL COUNT: " + service.getTotal(criteria));
		log.info("SEARCH ITEM LIST: " + service.getList(criteria));

		assertNotNull(item.getItemId());
		assertNotNull(savedItem);
		assertEquals(item.getItemCode(), savedItem.getItemCode());
	}

	// 品目の更新と利用停止を確認
	@Test
	public void testModifyAndChangeActive() {
		ItemVO item = createItem();
		service.register(item);

		String changedName = "Service更新テスト品目 " + createToken();

		item.setItemName(changedName);
		item.setOutboundPrice(new BigDecimal("25000"));

		log.info("========== SERVICE MODIFY TEST ==========");
		log.info("ITEM ID: " + item.getItemId());
		log.info("CHANGED ITEM NAME: " + changedName);

		boolean modified = service.modify(item);
		ItemVO changedItem = service.get(item.getItemId());

		log.info("MODIFY RESULT: " + modified);
		log.info("UPDATED ITEM: " + changedItem);

		assertTrue(modified);
		assertEquals(changedName, changedItem.getItemName());

		int changeCount = service.changeActive(Collections.singletonList(item.getItemId()), "N");

		ItemVO stoppedItem = service.get(item.getItemId());

		log.info("========== SERVICE ACTIVE TEST ==========");
		log.info("CHANGE COUNT: " + changeCount);
		log.info("ACTIVE FLAG: " + stoppedItem.getActiveFlag());

		assertEquals(1, changeCount);
		assertEquals("N", stoppedItem.getActiveFlag());
	}

	// 検索条件を生成
	private ItemCriteria createCriteria(String keyword) {
		ItemCriteria criteria = new ItemCriteria();

		criteria.setKeyword(keyword);
		criteria.setIncludeInactive("Y");

		return criteria;
	}

	// テスト品目を生成
	private ItemVO createItem() {
		String token = createToken();

		ItemVO item = new ItemVO();
		item.setItemCode("SERVICE" + token);
		item.setItemName("Serviceテスト品目 " + token);
		item.setSpecification("100mm");
		item.setUnit("EA");
		item.setItemType("商品");
		item.setInboundPrice(new BigDecimal("10000"));
		item.setOutboundPrice(new BigDecimal("15000"));
		item.setNote("Serviceテスト品目");

		return item;
	}

	// 重複防止用の文字列を生成
	private String createToken() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}
