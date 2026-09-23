package hexa.erp.assignee.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hexa.erp.assignee.service.AssigneeService;
import hexa.erp.common.domain.LookupCriteria;

@Controller
@RequestMapping("/lookup")
public class AssigneeLookupController {
	private AssigneeService assigneeService;

	@Autowired
	public void setAssigneeService(AssigneeService assigneeService) {
		this.assigneeService = assigneeService;
	}

	@GetMapping("/options/assignee")
	@ResponseBody
	public Map<String, Object> assigneeOptions(@RequestParam(name = "keyword", defaultValue = "") String keyword,
			@RequestParam(name = "page", defaultValue = "1") int page) {
		LookupCriteria criteria = new LookupCriteria();
		criteria.setKeyword(keyword);
		criteria.setPageNum(page);
		criteria.setAmount(25); // 선택 모달은 요청과 무관하게 25건씩 조회한다.

		// 조회 오류는 정상 0건과 구분해 그대로 전달한다.
		int totalCount = assigneeService.getTotal(criteria);
		int totalPages = (int) Math.max(1L, (totalCount + (long) criteria.getAmount() - 1) / criteria.getAmount());
		criteria.setPageNum(Math.min(criteria.getPageNum(), totalPages));

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("rows", assigneeService.getList(criteria));
		result.put("page", criteria.getPageNum());
		result.put("totalPages", totalPages);
		result.put("totalCount", totalCount);
		result.put("pageSize", criteria.getAmount());
		return result;
	}
}
