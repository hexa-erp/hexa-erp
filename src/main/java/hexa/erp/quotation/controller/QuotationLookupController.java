package hexa.erp.quotation.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.quotation.service.QuotationLookupService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/lookup")
public class QuotationLookupController {
	@Setter(onMethod_ = @Autowired)
	private QuotationLookupService quotationLookupService;

	@GetMapping("/sources/quotation")
	@ResponseBody
	public Map<String, Object> quotationSources(@RequestParam(name = "keyword", defaultValue = "") String keyword,
			@RequestParam(name = "progressStatus", defaultValue = "") String progressStatus,
			@RequestParam(name = "page", defaultValue = "1") int page) {
		log.info("견적서 불러오기 조회");
		LookupCriteria criteria = new LookupCriteria();
		criteria.setKeyword(keyword);
		criteria.setPageNum(page);
		criteria.setAmount(25);
		String status = progressStatus.trim();

		int totalCount = quotationLookupService.getTotal(criteria, status);
		int totalPages = (int) Math.max(1L, (totalCount + (long) criteria.getAmount() - 1) / criteria.getAmount());
		criteria.setPageNum(Math.min(criteria.getPageNum(), totalPages));

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("rows", quotationLookupService.getList(criteria, status));
		result.put("page", criteria.getPageNum());
		result.put("totalPages", totalPages);
		result.put("totalCount", totalCount);
		result.put("pageSize", criteria.getAmount());
		return result;
	}
}
