package hexa.erp.salesorder.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.salesorder.service.SalesOrderLookupService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/lookup")
public class SalesOrderLookupController {
	@Setter(onMethod_ = @Autowired)
	private SalesOrderLookupService salesOrderLookupService;

	@GetMapping("/sources/order")
	@ResponseBody
	public Map<String, Object> orderSources(@RequestParam(name = "keyword", defaultValue = "") String keyword,
			@RequestParam(name = "progressStatus", defaultValue = "") String progressStatus,
			@RequestParam(name = "page", defaultValue = "1") int page) {
		log.info("주문서 불러오기 조회");
		LookupCriteria criteria = new LookupCriteria();
		criteria.setKeyword(keyword);
		criteria.setPageNum(page);
		criteria.setAmount(25);
		String status = progressStatus.trim();

		int totalCount = salesOrderLookupService.getTotal(criteria, status);
		int totalPages = (int) Math.max(1L, (totalCount + (long) criteria.getAmount() - 1) / criteria.getAmount());
		criteria.setPageNum(Math.min(criteria.getPageNum(), totalPages));

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("rows", salesOrderLookupService.getList(criteria, status));
		result.put("page", criteria.getPageNum());
		result.put("totalPages", totalPages);
		result.put("totalCount", totalCount);
		result.put("pageSize", criteria.getAmount());
		return result;
	}
}
