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

	// 売上集計の月別グループ・小計・総合計を確認
	@Test
	public void testMonthlyAndOverallTotals() {
		SaleStatusRowVO januaryFirst = createRow("2026-01-05", "2", "1000", "2000", "200", "2200");

		SaleStatusRowVO januarySecond = createRow("2026-01-20", "3", "2000", "6000", "600", "6600");

		SaleStatusRowVO februaryRow = createRow("2026-02-10", "4", "500", "2000", "200", "2200");

		SaleStatusServiceImpl service = new SaleStatusServiceImpl();

		// DB取得の代わりに日付順の準備済みデータを返す
		service.setMapper(criteria -> Arrays.asList(januaryFirst, januarySecond, februaryRow));

		SaleStatusVO report = service.getStatus(new SaleStatusCriteria());

		assertNotNull(report);
		assertEquals(2, report.getMonthGroups().size());

		// 1月のグループと小計を確認
		Map<String, Object> january = report.getMonthGroups().get(0);

		assertEquals("2026-01", january.get("monthLabel"));
		assertEquals(Arrays.asList(januaryFirst, januarySecond), january.get("rows"));

		assertTotals((Map<?, ?>) january.get("totals"), "5", "3000", "8000", "800", "8800");

		// 2月のグループと小計を確認
		Map<String, Object> february = report.getMonthGroups().get(1);

		assertEquals("2026-02", february.get("monthLabel"));
		assertEquals(Arrays.asList(februaryRow), february.get("rows"));

		assertTotals((Map<?, ?>) february.get("totals"), "4", "500", "2000", "200", "2200");

		// 総合計を確認
		assertTotals(report.getTotals(), "9", "3500", "10000", "1000", "11000");
	}

	// 取得結果の明細行を生成
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

	// 小計または総合計を確認
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