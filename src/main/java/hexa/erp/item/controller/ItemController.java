package hexa.erp.item.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
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
import org.springframework.web.multipart.MultipartFile;
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
    public String list(
            ItemCriteria criteria,
            @RequestParam Map<String, String> params,
            HttpServletRequest request,
            Model model) {

        log.info("품목 목록 조회 요청");

        int totalCount = service.getTotal(criteria);
        int totalPages = Math.max(
            1,
            (int) Math.ceil(
                (double) totalCount / criteria.getPageSize()
            )
        );

        criteria.setPage(
            Math.min(criteria.getPage(), totalPages)
        );

        Map<String, String> search =
            ViewModels.search(params);

        search.put("keyword", criteria.getKeyword());
        search.put(
            "includeInactive",
            criteria.getIncludeInactive()
        );
        search.put(
            "page",
            String.valueOf(criteria.getPage())
        );
        search.put(
            "pageSize",
            String.valueOf(criteria.getPageSize())
        );

        List<ItemVO> itemList =
            service.getList(criteria);

        for (ItemVO item : itemList) {
            if (StringUtils.hasText(item.getImagePath())) {
                item.setImageUrl(
                    request.getContextPath()
                        + "/master/item/image?itemId="
                        + item.getItemId()
                );
            }
        }

        model.addAttribute("itemList", itemList);
        model.addAttribute("newItemCode", "");
        model.addAttribute("search", search);
        model.addAttribute("page", criteria.getPage());
        model.addAttribute(
            "pageSize",
            criteria.getPageSize()
        );
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute(
            "basePath",
            "/master/item"
        );
        model.addAttribute("activeMenu", "master");
        model.addAttribute("pageTitle", "품목 등록");
        model.addAttribute(
            "pageScript",
            "master.js"
        );
        model.addAttribute(
            "pageStyle",
            "master.css"
        );
        model.addAttribute(
            "warehouseOptions",
            stockService.getWarehouseList()
        );
        model.addAttribute(
            "stockList",
            stockService.getList()
        );

        return "master/item";
    }

    // 품목 등록과 수정 처리
    @PostMapping("/save")
    public String save(
            ItemVO item,
            BindingResult bindingResult,
            @RequestParam(
                value = "imageFile",
                required = false
            )
            MultipartFile imageFile,
            @RequestParam(
                value = "removeImage",
                defaultValue = "N"
            )
            String removeImage,
            HttpServletRequest request,
            RedirectAttributes rttr) {

        log.info("품목 저장 요청");

        if (bindingResult.hasErrors()) {
            rttr.addFlashAttribute(
                "errorMessage",
                "품목 입력값과 단가를 확인해 주세요."
            );

            return PostRedirects.toList(
                "/master/item",
                request
            );
        }

        if (!StringUtils.hasText(item.getItemName())
                || (item.getItemId() == null
                && !StringUtils.hasText(
                    item.getItemCode()
                ))) {

            rttr.addFlashAttribute(
                "errorMessage",
                "품목 코드와 품목명을 입력해 주세요."
            );

            return PostRedirects.toList(
                "/master/item",
                request
            );
        }

        List<String> itemTypes = Arrays.asList(
            "원재료",
            "부재료",
            "제품",
            "반제품",
            "상품",
            "무형상품"
        );

        if (!itemTypes.contains(item.getItemType())) {
            rttr.addFlashAttribute(
                "errorMessage",
                "품목 구분을 선택해 주세요."
            );

            return PostRedirects.toList(
                "/master/item",
                request
            );
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
                ItemVO stored =
                    service.get(item.getItemId());

                if (stored == null) {
                    rttr.addFlashAttribute(
                        "errorMessage",
                        "수정할 품목이 없습니다."
                    );

                    return PostRedirects.toList(
                        "/master/item",
                        request
                    );
                }

                oldImagePath = stored.getImagePath();
            }

            item.setImagePath(oldImagePath);

            if (imageFile != null
                    && !imageFile.isEmpty()) {

                uploadedPath =
                    imageService.store(imageFile);

                item.setImagePath(uploadedPath);

            } else if ("Y".equals(removeImage)) {
                item.setImagePath(null);
            }

            if (item.getItemId() == null) {
                item.setItemCode(
                    item.getItemCode().trim()
                );

                service.register(item);
                saved = true;

            } else {
                saved = service.modify(item);
            }

            if (!saved) {
                rttr.addFlashAttribute(
                    "errorMessage",
                    "수정할 품목이 없습니다."
                );

            } else if (!Objects.equals(
                    oldImagePath,
                    item.getImagePath())) {

                imageService.delete(oldImagePath);
            }

        } catch (DuplicateKeyException e) {
            rttr.addFlashAttribute(
                "errorMessage",
                "이미 등록된 품목 코드입니다. 다른 코드를 입력해 주세요."
            );

        } catch (DataAccessException e) {
            log.error("품목 저장 실패", e);

            rttr.addFlashAttribute(
                "errorMessage",
                "품목을 저장하지 못했습니다. 입력 내용을 확인해 주세요."
            );

        } catch (IOException e) {
            log.error("품목 사진 저장 실패", e);

            rttr.addFlashAttribute(
                "errorMessage",
                "사진을 저장하지 못했습니다. JPG·PNG·GIF 파일과 저장 폴더를 확인해 주세요."
            );

        } finally {
            if (!saved && uploadedPath != null) {
                imageService.delete(uploadedPath);
            }
        }

        return PostRedirects.toList(
            "/master/item",
            request
        );
    }

    // 품목 사용 여부 변경
    @PostMapping("/active")
    public String changeActive(
            @RequestParam(
                value = "ids",
                required = false
            )
            List<Long> ids,
            @RequestParam(
                value = "activeFlag",
                defaultValue = ""
            )
            String activeFlag,
            HttpServletRequest request,
            RedirectAttributes rttr) {

        log.info("품목 사용 여부 변경 요청");

        if (ids == null || ids.isEmpty()) {
            rttr.addFlashAttribute(
                "errorMessage",
                "처리할 품목을 선택해 주세요."
            );

        } else if (!"Y".equals(activeFlag)
                && !"N".equals(activeFlag)) {

            rttr.addFlashAttribute(
                "errorMessage",
                "사용 여부를 확인해 주세요."
            );

        } else {
            try {
                if (service.changeActive(
                        ids,
                        activeFlag) == 0) {

                    rttr.addFlashAttribute(
                        "errorMessage",
                        "변경할 품목이 없습니다."
                    );
                }

            } catch (DataAccessException e) {
                log.error(
                    "품목 사용 여부 변경 실패",
                    e
                );

                rttr.addFlashAttribute(
                    "errorMessage",
                    "품목의 사용 여부를 변경하지 못했습니다."
                );
            }
        }

        return PostRedirects.toList(
            "/master/item",
            request
        );
    }

    // 품목 현재고 조정
    @PostMapping("/stock")
    public String saveStock(
            StockVO stock,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes rttr) {

        log.info("품목 재고 저장 요청");

        if (bindingResult.hasErrors()
                || stock.getItemId() == null
                || stock.getWarehouseId() == null
                || stock.getQuantity() == null) {

            rttr.addFlashAttribute(
                "errorMessage",
                "품목·창고·입력 수량을 확인해 주세요."
            );

        } else {
            try {
                stockService.adjust(stock);

            } catch (DataAccessException e) {
                log.error("품목 재고 저장 실패", e);

                rttr.addFlashAttribute(
                    "errorMessage",
                    "재고를 저장하지 못했습니다. 품목과 창고를 확인해 주세요."
                );
            }
        }

        return PostRedirects.toList(
            "/master/item",
            request
        );
    }

    // 품목 이미지 응답
    @GetMapping("/image")
    @ResponseBody
    public ResponseEntity<byte[]> image(
            @RequestParam("itemId") Long itemId,
            @RequestParam(
                value = "thumbnail",
                defaultValue = "false"
            )
            boolean thumbnail) {

        ItemVO item = service.get(itemId);

        if (item == null
                || !StringUtils.hasText(
                    item.getImagePath()
                )) {

            return new ResponseEntity<>(
                HttpStatus.NOT_FOUND
            );
        }

        try {
            File file = imageService.getFile(
                item.getImagePath(),
                thumbnail
            );

            String name = file.getName()
                .toLowerCase(Locale.ROOT);

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
            headers.set(
                "X-Content-Type-Options",
                "nosniff"
            );

            return new ResponseEntity<>(
                FileCopyUtils.copyToByteArray(file),
                headers,
                HttpStatus.OK
            );

        } catch (IOException e) {
            log.warn(
                "품목 사진을 읽지 못했습니다: "
                    + itemId,
                e
            );

            return new ResponseEntity<>(
                HttpStatus.NOT_FOUND
            );
        }
    }
}
