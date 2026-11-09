package hexa.erp.quotation.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
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
import hexa.erp.quotation.domain.QuotationVO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
public class QuotationServiceTests {

	@Autowired
	private QuotationService service;

	@Autowired
	private ItemMapper itemMapper;

	// 数量・単価の丸めと金額計算を確認
	@Test
	public void testSaveCalculatesAmounts() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		QuotationVO quotation = createQuotation(item, "1.2346", "1000.555");

		Long quotationId = service.save(quotation);

		assertNotNull(quotationId);

		QuotationVO saved = service.get(quotationId);

		assertNotNull(saved);
		assertEquals(1, saved.getLines().size());

		QuotationLineVO line = saved.getLines().get(0);

		assertEquals(0, new BigDecimal("1.235").compareTo(line.getQuantity()));
		assertEquals(0, new BigDecimal("1000.56").compareTo(line.getUnitPrice()));
		assertEquals(0, new BigDecimal("1236").compareTo(line.getSupplyAmount()));
		assertEquals(0, new BigDecimal("124").compareTo(line.getVatAmount()));
		assertEquals(0, new BigDecimal("1360").compareTo(line.getTotalAmount()));
		assertEquals(0, new BigDecimal("1360").compareTo(saved.getTotalAmount()));
	}

	// 明細行の削除が対象の見積書だけに適用されることを確認
	@Test
	public void testRemoveSelectedLines() {
		ItemVO item = createItem();
		itemMapper.insertSelectKey(item);

		// 見積書A：明細行2件
		QuotationVO quotationA = createQuotation(item, "1", "1000");

		QuotationLineVO secondLine = new QuotationLineVO();
		secondLine.setItemId(item.getItemId());
		secondLine.setItemName(item.getItemName());
		secondLine.setSpecification(item.getSpecification());
		secondLine.setUnit(item.getUnit());
		secondLine.setQuantity(new BigDecimal("2"));
		secondLine.setUnitPrice(new BigDecimal("1000"));

		quotationA.getLines().add(secondLine);

		Long quotationAId = service.save(quotationA);

		// 見積書B：明細行1件
		QuotationVO quotationB = createQuotation(item, "3", "1000");
		Long quotationBId = service.save(quotationB);

		QuotationVO savedA = service.get(quotationAId);
		QuotationVO savedB = service.get(quotationBId);

		assertNotNull(savedA);
		assertNotNull(savedB);
		assertEquals(2, savedA.getLines().size());
		assertEquals(1, savedB.getLines().size());

		Long removeLineId = savedA.getLines().get(0).getQuotationLineId();
		Long keepLineId = savedA.getLines().get(1).getQuotationLineId();
		Long otherLineId = savedB.getLines().get(0).getQuotationLineId();

		// 保存する行から削除対象行を除外
		savedA.getLines().remove(0);

		// 別の見積書の行IDも混ぜて渡す
		savedA.setRemovedLineIds(Arrays.asList(removeLineId, otherLineId));

		service.save(savedA);

		QuotationVO changedA = service.get(quotationAId);
		QuotationVO unchangedB = service.get(quotationBId);

		// Aでは指定した行だけを削除し、残りの行を保持
		assertNotNull(changedA);
		assertEquals(1, changedA.getLines().size());
		assertEquals(keepLineId, changedA.getLines().get(0).getQuotationLineId());
		assertEquals(0, new BigDecimal("2200").compareTo(changedA.getTotalAmount()));

		// Bの行と金額を保持
		assertNotNull(unchangedB);
		assertEquals(1, unchangedB.getLines().size());
		assertEquals(otherLineId, unchangedB.getLines().get(0).getQuotationLineId());
		assertEquals(0, new BigDecimal("3300").compareTo(unchangedB.getTotalAmount()));
	}

	// テスト見積書を生成
	private QuotationVO createQuotation(ItemVO item, String quantity, String unitPrice) {

		QuotationVO quotation = new QuotationVO();
		quotation.setBusinessDate(LocalDate.now().toString());
		quotation.setNote("見積書Serviceテスト");

		QuotationLineVO line = new QuotationLineVO();
		line.setItemId(item.getItemId());
		line.setItemName(item.getItemName());
		line.setSpecification(item.getSpecification());
		line.setUnit(item.getUnit());
		line.setQuantity(new BigDecimal(quantity));
		line.setUnitPrice(new BigDecimal(unitPrice));

		quotation.getLines().add(line);

		return quotation;
	}

	// テスト品目を生成
	private ItemVO createItem() {
		String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

		ItemVO item = new ItemVO();
		item.setItemCode("QTEST" + token);
		item.setItemName("見積テスト品目 " + token);
		item.setSpecification("100mm");
		item.setUnit("EA");
		item.setItemType("商品");
		item.setInboundPrice(BigDecimal.ZERO);
		item.setOutboundPrice(new BigDecimal("1000.56"));

		return item;
	}
}