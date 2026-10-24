package hexa.erp.salesorder.mapper;

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

import hexa.erp.salesorder.domain.SalesOrderCriteria;
import hexa.erp.salesorder.domain.SalesOrderVO;
import lombok.extern.log4j.Log4j;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring/root-context.xml")
@Transactional
@Rollback
@Log4j
public class SalesOrderMapperTests {

	@Autowired
	private SalesOrderMapper mapper;

	// 일치하는 주문서가 없을 때 목록과 전체 건수 확인
	@Test
	public void testGetListWithPaging() {
		SalesOrderCriteria criteria = new SalesOrderCriteria();
		criteria.setKeyword("MAPPERTEST" + UUID.randomUUID().toString().replace("-", ""));

		int totalCount = mapper.getTotalCount(criteria);
		List<SalesOrderVO> list = mapper.getListWithPaging(criteria);

		log.info("========== SALES ORDER MAPPER LIST TEST ==========");
		log.info("SEARCH KEYWORD: " + criteria.getKeyword());
		log.info("TOTAL COUNT: " + totalCount);
		log.info("SALES ORDER LIST: " + list);

		assertEquals(0, totalCount);
		assertNotNull(list);
		assertTrue(list.isEmpty());
	}
}
