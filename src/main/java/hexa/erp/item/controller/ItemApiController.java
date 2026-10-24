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
			throw new ApiException(HttpStatus.NOT_FOUND, "조회할 품목이 없습니다.");
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
			throw new ApiException(HttpStatus.BAD_REQUEST, "품목 입력값과 단가를 확인해 주세요.");
		}
		if (item.getItemId() != null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "신규 품목에는 식별자를 지정할 수 없습니다.");
		}
		save(item, imageFile, removeImage);
		return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(item.getItemId()).toUri())
				.body(new ApiResponseVO("품목을 등록했습니다.", item.getItemId(), null));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponseVO update(@PathVariable("id") Long id, @ModelAttribute ItemVO item, BindingResult bindingResult,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			@RequestParam(value = "removeImage", defaultValue = "N") String removeImage) {
		if (bindingResult.hasErrors()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "품목 입력값과 단가를 확인해 주세요.");
		}
		item.setItemId(id);
		save(item, imageFile, removeImage);
		return new ApiResponseVO("품목을 수정했습니다.", id, null);
	}

	@PatchMapping(value = "/active", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponseVO changeActive(@RequestBody ActiveChangeVO change) {
		ApiRequests.requireIds(change);
		if (!"Y".equals(change.getActiveFlag()) && !"N".equals(change.getActiveFlag())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "사용 여부를 확인해 주세요.");
		}
		int count = service.changeActive(change.getIds(), change.getActiveFlag());
		if (count == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "변경할 품목이 없습니다.");
		}
		return new ApiResponseVO("품목의 사용 여부를 변경했습니다.", null, count);
	}

	// DB 저장이 성공한 경우에만 이전 사진을 삭제한다.
	private void save(ItemVO item, MultipartFile imageFile, String removeImage) {
		if (!StringUtils.hasText(item.getItemName())
				|| (item.getItemId() == null && !StringUtils.hasText(item.getItemCode()))) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "품목 코드와 품목명을 입력해 주세요.");
		}
		if (!Arrays.asList("원재료", "부재료", "제품", "반제품", "상품", "무형상품").contains(item.getItemType())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "품목 구분을 선택해 주세요.");
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
					throw new ApiException(HttpStatus.NOT_FOUND, "수정할 품목이 없습니다.");
				}
				oldImagePath = stored.getImagePath();
			}
			// 요청에 포함된 imagePath 대신 DB의 기존 경로를 사용한다.
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
				throw new ApiException(HttpStatus.NOT_FOUND, "수정할 품목이 없습니다.");
			}
			if (!Objects.equals(oldImagePath, item.getImagePath())) {
				imageService.delete(oldImagePath);
			}
		} catch (DuplicateKeyException e) {
			throw new ApiException(HttpStatus.CONFLICT, "이미 등록된 품목 코드입니다. 다른 코드를 입력해 주세요.");
		} catch (IOException e) {
			log.warn("품목 사진 저장 실패", e);
			throw new ApiException(HttpStatus.BAD_REQUEST,
					"사진을 저장하지 못했습니다. JPG·PNG·GIF 파일과 저장 폴더를 확인해 주세요.");
		} finally {
			if (!saved && uploadedPath != null) {
				imageService.delete(uploadedPath);
			}
		}
	}
}
