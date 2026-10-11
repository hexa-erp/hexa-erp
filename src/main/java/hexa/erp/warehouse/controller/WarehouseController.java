package hexa.erp.warehouse.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import hexa.erp.common.controller.PostRedirects;
import hexa.erp.common.controller.ViewModels;
import hexa.erp.warehouse.domain.WarehouseCriteria;
import hexa.erp.warehouse.domain.WarehouseVO;
import hexa.erp.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/master/warehouse")
public class WarehouseController {

    // 창고 Service 연결
    private final WarehouseService service;

    // 창고 목록 조회
    @GetMapping
    public String list(
            WarehouseCriteria criteria,
            @RequestParam Map<String, String> params,
            Model model) {

        log.info("창고 목록 조회 요청");

        int totalCount = service.getTotal(criteria);
        int totalPages = Math.max(
            1,
            (int) Math.ceil((double) totalCount / criteria.getPageSize())
        );

        criteria.setPage(Math.min(criteria.getPage(), totalPages));

        Map<String, String> search = ViewModels.search(params);
        search.put("keyword", criteria.getKeyword());
        search.put("includeInactive", criteria.getIncludeInactive());
        search.put("page", String.valueOf(criteria.getPage()));
        search.put("pageSize", String.valueOf(criteria.getPageSize()));

        model.addAttribute("warehouseList", service.getList(criteria));
        model.addAttribute("newWarehouseCode", "");
        model.addAttribute("search", search);
        model.addAttribute("page", criteria.getPage());
        model.addAttribute("pageSize", criteria.getPageSize());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("basePath", "/master/warehouse");
        model.addAttribute("activeMenu", "master");
        model.addAttribute("pageTitle", "창고 등록");
        model.addAttribute("pageScript", "master.js");
        model.addAttribute("pageStyle", "master.css");

        return "master/warehouse";
    }

    // 창고 등록과 수정 처리
    @PostMapping("/save")
    public String save(
            WarehouseVO warehouse,
            HttpServletRequest request,
            RedirectAttributes rttr) {

        log.info("창고 저장 요청");

        if (!StringUtils.hasText(warehouse.getWarehouseName())
                || (warehouse.getWarehouseId() == null
                && !StringUtils.hasText(warehouse.getWarehouseCode()))) {

            rttr.addFlashAttribute(
                "errorMessage",
                "창고 코드와 창고명을 입력해 주세요."
            );

            return PostRedirects.toList("/master/warehouse", request);
        }

        if (!"창고".equals(warehouse.getWarehouseType())
                && !"공장".equals(warehouse.getWarehouseType())) {

            rttr.addFlashAttribute(
                "errorMessage",
                "창고 구분을 선택해 주세요."
            );

            return PostRedirects.toList("/master/warehouse", request);
        }

        warehouse.setWarehouseName(warehouse.getWarehouseName().trim());

        try {
            if (warehouse.getWarehouseId() == null) {
                warehouse.setWarehouseCode(
                    warehouse.getWarehouseCode().trim()
                );
                service.register(warehouse);

            } else if (!service.modify(warehouse)) {
                rttr.addFlashAttribute(
                    "errorMessage",
                    "수정할 창고가 없습니다."
                );
            }

        } catch (DuplicateKeyException e) {
            rttr.addFlashAttribute(
                "errorMessage",
                "이미 등록된 창고 코드입니다. 다른 코드를 입력해 주세요."
            );

        } catch (DataAccessException e) {
            log.error("창고 저장 실패", e);

            rttr.addFlashAttribute(
                "errorMessage",
                "창고를 저장하지 못했습니다. 입력 내용을 확인해 주세요."
            );
        }

        return PostRedirects.toList("/master/warehouse", request);
    }

    // 창고 사용 여부 변경
    @PostMapping("/active")
    public String changeActive(
            @RequestParam(value = "ids", required = false) List<Long> ids,
            @RequestParam(value = "activeFlag", defaultValue = "") String activeFlag,
            HttpServletRequest request,
            RedirectAttributes rttr) {

        log.info("창고 사용 여부 변경 요청");

        if (ids == null || ids.isEmpty()) {
            rttr.addFlashAttribute(
                "errorMessage",
                "처리할 창고를 선택해 주세요."
            );

        } else if (!"Y".equals(activeFlag) && !"N".equals(activeFlag)) {
            rttr.addFlashAttribute(
                "errorMessage",
                "사용 여부를 확인해 주세요."
            );

        } else {
            try {
                if (service.changeActive(ids, activeFlag) == 0) {
                    rttr.addFlashAttribute(
                        "errorMessage",
                        "변경할 창고가 없습니다."
                    );
                }

            } catch (DataAccessException e) {
                log.error("창고 사용 여부 변경 실패", e);

                rttr.addFlashAttribute(
                    "errorMessage",
                    "창고의 사용 여부를 변경하지 못했습니다."
                );
            }
        }

        return PostRedirects.toList("/master/warehouse", request);
    }
}
