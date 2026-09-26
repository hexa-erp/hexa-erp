package hexa.erp.shipinstruction.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hexa.erp.common.domain.LookupCriteria;
import hexa.erp.shipinstruction.service.ShipInstructionLookupService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/lookup")
public class ShipInstructionLookupController {
	@Setter(onMethod_ = @Autowired)
	private ShipInstructionLookupService shipInstructionLookupService;

	@GetMapping("/sources/shipping-instruction")
	@ResponseBody
	public Map<String, Object> shipInstructionSources(@RequestParam(name = "keyword", defaultValue = "") String keyword,
			@RequestParam(name = "progressStatus", defaultValue = "") String progressStatus,
			@RequestParam(name = "page", defaultValue = "1") int page) {
		log.info("출하지시서 불러오기 조회");
		LookupCriteria criteria = new LookupCriteria();
		criteria.setKeyword(keyword);
		criteria.setPageNum(page);
		criteria.setAmount(25);
		progressStatus = progressStatus.trim();

		int totalCount = shipInstructionLookupService.getTotal(criteria, progressStatus);
		int totalPages = (int) Math.max(1L, (totalCount + (long) criteria.getAmount() - 1) / criteria.getAmount());
		criteria.setPageNum(Math.min(criteria.getPageNum(), totalPages));

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("rows", shipInstructionLookupService.getList(criteria, progressStatus));
		result.put("page", criteria.getPageNum());
		result.put("totalPages", totalPages);
		result.put("totalCount", totalCount);
		result.put("pageSize", criteria.getAmount());
		return result;
	}
}
