package hexa.erp.sale.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hexa.erp.common.controller.PostRedirects;
import hexa.erp.common.controller.ViewModels;
import hexa.erp.common.service.FilterSelectionService;
import hexa.erp.sale.domain.SalePriceCriteria;
import hexa.erp.sale.domain.SalePriceFormVO;
import hexa.erp.sale.domain.SalePriceVO;
import hexa.erp.sale.service.SalePriceService;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/sale/bulk-price")
public class SalePriceController {
	@Setter(onMethod_ = @Autowired)
	private SalePriceService service;
	@Setter(onMethod_ = @Autowired)
	private FilterSelectionService filterSelectionService;

	@GetMapping
	public String bulkPrice(SalePriceCriteria criteria, BindingResult bindingResult,
			@RequestParam Map<String, String> params, HttpServletRequest request, Model model) {
		log.info("판매 단가 일괄 변경 화면 요청");
		Map<String, String> search = ViewModels.search(params);
		// 창고·거래처·품목만 검색조건으로 받는다.
		Map<String, List<String>> filterIds = ViewModels.filters(request, search, model, "warehouse", "partner",
				"item");
		// 필터 ID는 같은 이름의 단일 ID 파라미터까지 합친 값으로 덮어쓴다.
		criteria.setWarehouseIds(filterIds.get("warehouseIds"));
		criteria.setPartnerIds(filterIds.get("partnerIds"));
		criteria.setItemIds(filterIds.get("itemIds"));
		List<SalePriceVO> rows = Collections.emptyList();
		int totalCount = 0;
		int totalPages = 1;
		// 검색조건 입력 화면(view 없음)에서는 조회하지 않고, 결과 화면에서만 조회한다.
		if ("results".equals(params.get("view"))) {
			if (bindingResult.hasErrors()) {
				model.addAttribute("errorMessage", "검색조건과 페이지 번호를 확인해 주세요.");
			} else {
				try {
					totalCount = service.getTotal(criteria);
					totalPages = Math.max(1, (int) Math.ceil((double) totalCount / criteria.getPageSize()));
					// 저장 후 행이 줄어 현재 페이지가 사라졌으면 마지막 페이지를 보여준다.
					criteria.setPage(Math.min(criteria.getPage(), totalPages));
					rows = service.getList(criteria);
				} catch (DataAccessException e) {
					log.error("판매 단가 일괄 변경 조회 실패", e);
					model.addAttribute("errorMessage", "판매 상세행을 조회하지 못했습니다. 검색조건을 확인해 주세요.");
				}
			}
		}
		// 화면에는 Criteria에서 보정한 값(공백 제거, 페이지 범위)을 다시 보여준다.
		search.put("keyword", criteria.getKeyword());
		search.put("progressStatus", criteria.getProgressStatus());
		search.put("page", String.valueOf(criteria.getPage()));
		search.put("pageSize", String.valueOf(criteria.getPageSize()));
		model.addAttribute("filterSelections", filterSelectionService.getSelections(filterIds));
		model.addAttribute("search", search);
		model.addAttribute("salePriceList", rows);
		model.addAttribute("page", criteria.getPage());
		model.addAttribute("pageSize", criteria.getPageSize());
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("basePath", "/sale/bulk-price");
		model.addAttribute("activeMenu", "sale");
		model.addAttribute("pageTitle", "판매 단가 일괄 변경");
		model.addAttribute("pageScript", "sale.js");
		return "sale/bulk-price";
	}

	@PostMapping("/save")
	public String savePrices(SalePriceFormVO form, BindingResult bindingResult, HttpServletRequest request,
			RedirectAttributes rttr) {
		log.info("판매 단가 일괄 저장 요청");
		// 단가에 숫자가 아닌 값이 들어오면 바인딩 오류가 난다.
		if (bindingResult.hasErrors()) {
			rttr.addFlashAttribute("errorMessage", "판매 상세행과 단가의 입력값을 확인해 주세요.");
		} else {
			try {
				service.savePrices(form.getChanges());
			} catch (IllegalArgumentException e) {
				rttr.addFlashAttribute("errorMessage", e.getMessage());
			} catch (DataAccessException e) {
				log.error("판매 단가 일괄 저장 실패", e);
				rttr.addFlashAttribute("errorMessage", "판매 단가를 저장하지 못했습니다. 입력값을 확인한 뒤 다시 저장해 주세요.");
			}
		}
		// 성공·실패와 관계없이 저장 전 검색조건(return.*)의 결과 화면으로 돌아간다.
		return PostRedirects.afterPriceChange(request);
	}
}
