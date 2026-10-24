package hexa.erp.item.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import hexa.erp.common.controller.ApiException;
import hexa.erp.common.controller.ApiRequests;
import hexa.erp.common.domain.ActiveChangeVO;
import hexa.erp.common.domain.ApiResponseVO;
import hexa.erp.item.domain.ItemVO;
import hexa.erp.item.service.ItemImageService;
import hexa.erp.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Log4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/items", produces = MediaType.APPLICATION_JSON_VALUE)
public class ItemApiController {

	private final ItemService service;
	private final ItemImageService imageService;

	@GetMapping("/{id}")
	public ItemVO get(@PathVariable("id") Long id, HttpServletRequest request) {
		ItemVO item = service.get(id);
		if (item == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, "表示対象の品目がありません。");
		}
		if (StringUtils.hasText(item.getImagePath())) {
			item.setImageUrl(request.getContextPath() + "/master/item/image?itemId=" + id);
		}
		return item;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponseVO> create(@ModelAttribute ItemVO item, BindingResult bindingResult,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			@RequestParam(value = "removeImage", defaultValue = "N") String removeImage) {
		if (bindingResult.hasErrors()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "品目の入力内容と単価を確認してください。");
		}
		if (item.getItemId() != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "新規品目にIDを指定することはできません。");
		}
		save(item, imageFile, removeImage);
		return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(item.getItemId()).toUri())
				.body(new ApiResponseVO("品目を登録しました。", item.getItemId(), null));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponseVO update(@PathVariable("id") Long id, @ModelAttribute ItemVO item, BindingResult bindingResult,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			@RequestParam(value = "removeImage", defaultValue = "N") String removeImage) {
		if (bindingResult.hasErrors()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "品目の入力内容と単価を確認してください。");
		}
		item.setItemId(id);
		save(item, imageFile, removeImage);
		return new ApiResponseVO("品目を更新しました。", id, null);
	}

	@PatchMapping(value = "/active", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseVO changeActive(@RequestBody ActiveChangeVO change) {
		ApiRequests.requireIds(change);
		if (!"Y".equals(change.getActiveFlag()) && !"N".equals(change.getActiveFlag())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "利用状態を確認してください。");
		}
		int count = service.changeActive(change.getIds(), change.getActiveFlag());
		if (count == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "変更対象の品目がありません。");
		}
		return new ApiResponseVO("品目の利用状態を変更しました。", null, count);
	}

	// DBへの保存が成功した場合のみ、変更前の画像を削除する。
	private void save(ItemVO item, MultipartFile imageFile, String removeImage) {
		if (!StringUtils.hasText(item.getItemName())
				|| (item.getItemId() == null && !StringUtils.hasText(item.getItemCode()))) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "品目コードと品目名を入力してください。");
		}
		if (!Arrays.asList("原材料", "副資材", "製品", "半製品", "商品", "無形商品").contains(item.getItemType())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "品目区分を選択してください。");
		}
		item.setItemName(item.getItemName().trim());
		if (item.getInboundPrice() == null) {
			item.setInboundPrice(BigDecimal.ZERO);
		}
		if (item.getOutboundPrice() == null) {
			item.setOutboundPrice(BigDecimal.ZERO);
		}

		String uploadedPath = null;
		boolean saved = false;
		try {
			String oldImagePath = null;
			if (item.getItemId() != null) {
				ItemVO stored = service.get(item.getItemId());
				if (stored == null) {
					throw new ApiException(HttpStatus.NOT_FOUND, "更新対象の品目がありません。");
				}
				oldImagePath = stored.getImagePath();
			}
			// リクエストのimagePathではなく、DBに保存されているパスを使用する。
			item.setImagePath(oldImagePath);
			if (imageFile != null && !imageFile.isEmpty()) {
				uploadedPath = imageService.store(imageFile);
				item.setImagePath(uploadedPath);
			} else if ("Y".equals(removeImage)) {
				item.setImagePath(null);
			}

			if (item.getItemId() == null) {
				item.setItemCode(item.getItemCode().trim());
				service.register(item);
				saved = true;
			} else {
				saved = service.modify(item);
			}
			if (!saved) {
				throw new ApiException(HttpStatus.NOT_FOUND, "更新対象の品目がありません。");
			}
			if (!Objects.equals(oldImagePath, item.getImagePath())) {
				imageService.delete(oldImagePath);
			}
		} catch (DuplicateKeyException e) {
			throw new ApiException(HttpStatus.CONFLICT, "登録済みの品目コードです。別のコードを入力してください。");
		} catch (IOException e) {
			log.warn("品目画像の保存に失敗しました", e);
			throw new ApiException(HttpStatus.BAD_REQUEST,
					"画像を保存できませんでした。JPG・PNG・GIFファイルと保存フォルダーを確認してください。");
		} finally {
			if (!saved && uploadedPath != null) {
				imageService.delete(uploadedPath);
			}
		}
	}
}
