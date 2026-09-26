package hexa.erp.warehouse.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hexa.erp.warehouse.service.WarehouseLookupService;
import hexa.erp.common.domain.LookupCriteria;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/lookup")
public class WarehouseLookupController {
	@Setter(onMethod_ = @Autowired)
	private WarehouseLookupService warehouseLookupService;

	@GetMapping("/options/warehouse")
	@ResponseBody
	public Map<String, Object> warehouseOptions(@RequestParam(name = "keyword", defaultValue = "") String keyword,
			@RequestParam(name = "page", defaultValue = "1") int page) {
		log.info("창고 선택 목록 조회");
		LookupCriteria criteria = new LookupCriteria();
		criteria.setKeyword(keyword);
		criteria.setPageNum(page);
		criteria.setAmount(25);

		int totalCount = warehouseLookupService.getTotal(criteria);
		int totalPages = (int) Math.max(1L, (totalCount + (long) criteria.getAmount() - 1) / criteria.getAmount());
		criteria.setPageNum(Math.min(criteria.getPageNum(), totalPages));

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("rows", warehouseLookupService.getList(criteria));
		result.put("page", criteria.getPageNum());
		result.put("totalPages", totalPages);
		result.put("totalCount", totalCount);
		result.put("pageSize", criteria.getAmount());
		return result;
	}
}
