package hexa.erp.sale.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import hexa.erp.sale.domain.SaleStatusCriteria;
import hexa.erp.sale.domain.SaleStatusRowVO;
import hexa.erp.sale.domain.SaleStatusVO;

public class SaleStatusServiceTests {

	// 판매 현황의 월별 그룹·소계·전체 합계 확인
	@Test
	public void testMonthlyAndOverallTotals() {
		SaleStatusRowVO januaryFirst = createRow("2026-01-05", "2", "1000", "2000", "200", "2200");

		SaleStatusRowVO januarySecond = createRow("2026-01-20", "3", "2000", "6000", "600", "6600");

		SaleStatusRowVO februaryRow = createRow("2026-02-10", "4", "500", "2000", "200", "2200");

		SaleStatusServiceImpl service = new SaleStatusServiceImpl();

		// DB 조회 대신 준비한 날짜순 데이터를 반환
		service.setMapper(criteria -> Arrays.asList(januaryFirst, januarySecond, februaryRow));

		SaleStatusVO report = service.getStatus(new SaleStatusCriteria());

		assertNotNull(report);
		assertEquals(2, report.getMonthGroups().size());

		// 1월 그룹과 소계 확인
		Map<String, Object> january = report.getMonthGroups().get(0);

		assertEquals("2026-01", january.get("monthLabel"));
		assertEquals(Arrays.asList(januaryFirst, januarySecond), january.get("rows"));

		assertTotals((Map<?, ?>) january.get("totals"), "5", "3000", "8000", "800", "8800");

		// 2월 그룹과 소계 확인
		Map<String, Object> february = report.getMonthGroups().get(1);

		assertEquals("2026-02", february.get("monthLabel"));
		assertEquals(Arrays.asList(februaryRow), february.get("rows"));

		assertTotals((Map<?, ?>) february.get("totals"), "4", "500", "2000", "200", "2200");

		// 전체 합계 확인
		assertTotals(report.getTotals(), "9", "3500", "10000", "1000", "11000");
	}

	// 조회 결과의 상세행 생성
	private SaleStatusRowVO createRow(String businessDate, String quantity, String unitPrice, String supplyAmount,
			String vatAmount, String totalAmount) {

		SaleStatusRowVO row = new SaleStatusRowVO();
		row.setBusinessDate(businessDate);
		row.setQuantity(new BigDecimal(quantity));
		row.setUnitPrice(new BigDecimal(unitPrice));
		row.setSupplyAmount(new BigDecimal(supplyAmount));
		row.setVatAmount(new BigDecimal(vatAmount));
		row.setTotalAmount(new BigDecimal(totalAmount));

		return row;
	}

	// 소계 또는 전체 합계 확인
	private void assertTotals(Map<?, ?> totals, String quantity, String unitPrice, String supplyAmount,
			String vatAmount, String totalAmount) {

		assertNotNull(totals);

		assertDecimal(quantity, (BigDecimal) totals.get("quantity"));
		assertDecimal(unitPrice, (BigDecimal) totals.get("unitPrice"));
		assertDecimal(supplyAmount, (BigDecimal) totals.get("supplyAmount"));
		assertDecimal(vatAmount, (BigDecimal) totals.get("vatAmount"));
		assertDecimal(totalAmount, (BigDecimal) totals.get("totalAmount"));
	}

	private void assertDecimal(String expected, BigDecimal actual) {
		assertNotNull(actual);
		assertEquals(0, new BigDecimal(expected).compareTo(actual));
	}
}