package hexa.erp.sale.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.sale.service.SaleLookupService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/lookup")
public class SaleLookupController {
	@Setter(onMethod_ = @Autowired)
	private SaleLookupService saleLookupService;

	@GetMapping("/sources/sale")
	@ResponseBody
	public Map<String, Object> saleSources(@RequestParam(name = "keyword", defaultValue = "") String keyword,
			@RequestParam(name = "progressStatus", defaultValue = "") String progressStatus,
			@RequestParam(name = "page", defaultValue = "1") int page) {
		log.info("판매 불러오기 조회");
		LookupCriteria criteria = new LookupCriteria();
		criteria.setKeyword(keyword);
		criteria.setPageNum(page);
		criteria.setAmount(25);
		progressStatus = progressStatus.trim();

		int totalCount = saleLookupService.getTotal(criteria, progressStatus);
		int totalPages = (int) Math.max(1L, (totalCount + (long) criteria.getAmount() - 1) / criteria.getAmount());
		criteria.setPageNum(Math.min(criteria.getPageNum(), totalPages));

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("rows", saleLookupService.getList(criteria, progressStatus));
		result.put("page", criteria.getPageNum());
		result.put("totalPages", totalPages);
		result.put("totalCount", totalCount);
		result.put("pageSize", criteria.getAmount());
		return result;
	}
}
