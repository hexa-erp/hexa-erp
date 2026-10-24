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

	// 品目Serviceの参照
	private final ItemService service;

	// 在庫Serviceの参照
	private final StockService stockService;

	// 品目画像Serviceの参照
	private final ItemImageService imageService;

	// 品目一覧を取得する。
	@GetMapping
	public String list(ItemCriteria criteria, @RequestParam Map<String, String> params, HttpServletRequest request,
			Model model) {

		log.info("品目一覧の取得リクエスト");

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
		model.addAttribute("pageTitle", "品目登録");
		model.addAttribute("pageScript", "master.js");
		model.addAttribute("pageStyle", "master.css");
		model.addAttribute("warehouseOptions", stockService.getWarehouseList());
		model.addAttribute("stockList", stockService.getList());

		return "master/item";
	}

	// 品目の現在庫を調整する。
	@PostMapping("/stock")
	public String saveStock(StockVO stock, BindingResult bindingResult, HttpServletRequest request,
			RedirectAttributes rttr) {

		log.info("品目在庫の保存リクエスト");

		if (bindingResult.hasErrors() || stock.getItemId() == null || stock.getWarehouseId() == null
				|| stock.getQuantity() == null) {

			rttr.addFlashAttribute("errorMessage", "品目・倉庫・入力数量を確認してください。");

		} else {
			try {
				stockService.adjust(stock);

			} catch (DataAccessException e) {
				log.error("品目在庫の保存に失敗しました", e);

				rttr.addFlashAttribute("errorMessage", "在庫を保存できませんでした。品目と倉庫を確認してください。");
			}
		}

		return PostRedirects.toList("/master/item", request);
	}

	// 品目画像のレスポンス
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
			log.warn("品目画像を読み込めませんでした: " + itemId, e);

			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
