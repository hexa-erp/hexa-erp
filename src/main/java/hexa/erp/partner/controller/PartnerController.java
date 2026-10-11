package hexa.erp.partner.controller;

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
import hexa.erp.partner.domain.PartnerCriteria;
import hexa.erp.partner.domain.PartnerVO;
import hexa.erp.partner.service.PartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/master/partner")
public class PartnerController {

    private final PartnerService service;

    @GetMapping
    public String list(
            PartnerCriteria criteria,
            @RequestParam Map<String, String> params,
            Model model) {

        log.info("거래처 목록 조회 요청");

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

        model.addAttribute("partnerList", service.getList(criteria));
        model.addAttribute("newPartnerCode", "");
        model.addAttribute("search", search);
        model.addAttribute("page", criteria.getPage());
        model.addAttribute("pageSize", criteria.getPageSize());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("basePath", "/master/partner");
        model.addAttribute("activeMenu", "master");
        model.addAttribute("pageTitle", "거래처 등록");
        model.addAttribute("pageScript", "master.js");
        model.addAttribute("pageStyle", "master.css");

        return "master/partner";
    }

    @PostMapping("/save")
    public String save(
            PartnerVO partner,
            HttpServletRequest request,
            RedirectAttributes rttr) {

        log.info("거래처 저장 요청");

        if (!StringUtils.hasText(partner.getPartnerName())
                || (partner.getPartnerId() == null
                && !StringUtils.hasText(partner.getPartnerCode()))) {

            rttr.addFlashAttribute(
                "errorMessage",
                "거래처 코드와 상호를 입력해 주세요."
            );

            return PostRedirects.toList("/master/partner", request);
        }

        partner.setPartnerName(partner.getPartnerName().trim());

        try {
            if (partner.getPartnerId() == null) {
                partner.setPartnerCode(partner.getPartnerCode().trim());
                service.register(partner);

            } else if (!service.modify(partner)) {
                rttr.addFlashAttribute(
                    "errorMessage",
                    "수정할 거래처가 없습니다."
                );
            }

        } catch (DuplicateKeyException e) {
            rttr.addFlashAttribute(
                "errorMessage",
                "이미 등록된 거래처 코드입니다. 다른 코드를 입력해 주세요."
            );

        } catch (DataAccessException e) {
            log.error("거래처 저장 실패", e);

            rttr.addFlashAttribute(
                "errorMessage",
                "거래처를 저장하지 못했습니다. 입력 내용을 확인해 주세요."
            );
        }

        return PostRedirects.toList("/master/partner", request);
    }

    @PostMapping("/active")
    public String changeActive(
            @RequestParam(value = "ids", required = false) List<Long> ids,
            @RequestParam(value = "activeFlag", defaultValue = "") String activeFlag,
            HttpServletRequest request,
            RedirectAttributes rttr) {

        log.info("거래처 사용 여부 변경 요청");

        if (ids == null || ids.isEmpty()) {
            rttr.addFlashAttribute(
                "errorMessage",
                "처리할 거래처를 선택해 주세요."
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
                        "변경할 거래처가 없습니다."
                    );
                }

            } catch (DataAccessException e) {
                log.error("거래처 사용 여부 변경 실패", e);

                rttr.addFlashAttribute(
                    "errorMessage",
                    "거래처의 사용 여부를 변경하지 못했습니다."
                );
            }
        }

        return PostRedirects.toList("/master/partner", request);
    }
}
