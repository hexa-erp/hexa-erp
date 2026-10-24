package hexa.erp.item.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hexa.erp.common.controller.PostRedirects;
import hexa.erp.common.controller.ViewModels;
import hexa.erp.item.domain.ItemCriteria;
import hexa.erp.item.domain.ItemVO;
import hexa.erp.item.service.ItemImageService;
import hexa.erp.item.service.ItemService;
import hexa.erp.stock.domain.StockVO;
import hexa.erp.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/master/item")
public class ItemController {

	// 품목 Service 연결
	private final ItemService service;

	// 재고 Service 연결
	private final StockService stockService;

	// 품목 이미지 Service 연결
	private final ItemImageService imageService;

	// 품목 목록 조회
	@GetMapping
	public String list(ItemCriteria criteria, @RequestParam Map<String, String> params, HttpServletRequest request,
			Model model) {

		log.info("품목 목록 조회 요청");

		int totalCount = service.getTotal(criteria);
		int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / criteria.getPageSize()));

		criteria.setPage(Math.min(criteria.getPage(), totalPages));

		Map<String, String> search = ViewModels.search(params);

		search.put("keyword", criteria.getKeyword());
		search.put("includeInactive", criteria.getIncludeInactive());
		search.put("page", String.valueOf(criteria.getPage()));
		search.put("pageSize", String.valueOf(criteria.getPageSize()));

		List<ItemVO> itemList = service.getList(criteria);

		for (ItemVO item : itemList) {
			if (StringUtils.hasText(item.getImagePath())) {
				item.setImageUrl(request.getContextPath() + "/master/item/image?itemId=" + item.getItemId());
			}
		}

		model.addAttribute("itemList", itemList);
		model.addAttribute("newItemCode", "");
		model.addAttribute("search", search);
		model.addAttribute("page", criteria.getPage());
		model.addAttribute("pageSize", criteria.getPageSize());
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("totalCount", totalCount);
		model.addAttribute("basePath", "/master/item");
		model.addAttribute("activeMenu", "master");
		model.addAttribute("pageTitle", "품목 등록");
		model.addAttribute("pageScript", "master.js");
		model.addAttribute("pageStyle", "master.css");
		model.addAttribute("warehouseOptions", stockService.getWarehouseList());
		model.addAttribute("stockList", stockService.getList());

		return "master/item";
	}

	// 품목 현재고 조정
	@PostMapping("/stock")
	public String saveStock(StockVO stock, BindingResult bindingResult, HttpServletRequest request,
			RedirectAttributes rttr) {

		log.info("품목 재고 저장 요청");

		if (bindingResult.hasErrors() || stock.getItemId() == null || stock.getWarehouseId() == null
				|| stock.getQuantity() == null) {

			rttr.addFlashAttribute("errorMessage", "품목·창고·입력 수량을 확인해 주세요.");

		} else {
			try {
				stockService.adjust(stock);

			} catch (DataAccessException e) {
				log.error("품목 재고 저장 실패", e);

				rttr.addFlashAttribute("errorMessage", "재고를 저장하지 못했습니다. 품목과 창고를 확인해 주세요.");
			}
		}

		return PostRedirects.toList("/master/item", request);
	}

	// 품목 이미지 응답
	@GetMapping("/image")
	@ResponseBody
	public ResponseEntity<byte[]> image(@RequestParam("itemId") Long itemId,
			@RequestParam(value = "thumbnail", defaultValue = "false") boolean thumbnail) {

		ItemVO item = service.get(itemId);

		if (item == null || !StringUtils.hasText(item.getImagePath())) {

			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		try {
			File file = imageService.getFile(item.getImagePath(), thumbnail);

			String name = file.getName().toLowerCase(Locale.ROOT);

			MediaType contentType;

			if (name.endsWith(".png")) {
				contentType = MediaType.IMAGE_PNG;

			} else if (name.endsWith(".gif")) {
				contentType = MediaType.IMAGE_GIF;

			} else {
				contentType = MediaType.IMAGE_JPEG;
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(contentType);
			headers.setCacheControl("no-store");
			headers.set("X-Content-Type-Options", "nosniff");

			return new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), headers, HttpStatus.OK);

		} catch (IOException e) {
			log.warn("품목 사진을 읽지 못했습니다: " + itemId, e);

			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
