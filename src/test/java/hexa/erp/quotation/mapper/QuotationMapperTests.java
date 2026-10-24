package hexa.erp.quotation.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import hexa.erp.quotation.domain.QuotationCriteria;
import hexa.erp.quotation.domain.QuotationVO;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
@Log4j
public class QuotationMapperTests {

	@Autowired
	private QuotationMapper mapper;

	// 一致する見積書がない場合の一覧と総件数を確認
	@Test
	public void testGetListWithPaging() {
		QuotationCriteria criteria = new QuotationCriteria();
		criteria.setKeyword("MAPPERTEST" + UUID.randomUUID().toString().replace("-", ""));

		int totalCount = mapper.getTotalCount(criteria);
		List<QuotationVO> list = mapper.getListWithPaging(criteria);

		log.info("========== QUOTATION MAPPER LIST TEST ==========");
		log.info("SEARCH KEYWORD: " + criteria.getKeyword());
		log.info("TOTAL COUNT: " + totalCount);
		log.info("QUOTATION LIST: " + list);

		assertEquals(0, totalCount);
		assertNotNull(list);
		assertTrue(list.isEmpty());
	}
}
