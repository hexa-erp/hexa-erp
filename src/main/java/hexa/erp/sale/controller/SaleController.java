package hexa.erp.sale.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import hexa.erp.sale.domain.SaleVO;
import hexa.erp.sale.service.SaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

@Log4j
@Controller
@RequestMapping("/sale")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService service;

    // 판매 목록 화면
    @GetMapping("/list")
    public String list(
            @RequestParam Map<String, String> params,
            Model model) {

        log.info("판매 목록 조회 요청");

        model.addAttribute(
            "saleList",
            Collections.emptyList()
        );
        model.addAttribute(
            "search",
            ViewModels.search(params)
        );
        model.addAttribute("activeMenu", "sale");
        model.addAttribute("pageTitle", "판매 조회");

        ViewModels.emptyPage(
            model,
            25,
            "/sale/list"
        );

        return "sale/list";
    }

    // 판매 입력 화면
    @GetMapping("/form")
    public String form(
            @RequestParam(
                value = "id",
                required = false
            ) Long id,
            @RequestParam(
                value = "saleId",
                required = false
            ) Long saleId,
            HttpServletRequest request,
            Model model,
            RedirectAttributes rttr) {

        log.info("판매 입력 화면 요청");

        Long documentId =
            id == null ? saleId : id;

        SaleVO form;

        if (documentId == null) {
            form = new SaleVO();
            form.setBusinessDate(
                LocalDate.now().toString()
            );
            form.setProgressStatus("CONFIRMED");
        } else {
            form = service.get(documentId);

            if (form == null) {
                rttr.addFlashAttribute(
                    "errorMessage",
                    "조회할 판매가 없습니다."
                );

                return PostRedirects.toList(
                    "/sale/list",
                    request
                );
            }
        }

        model.addAttribute("form", form);
        model.addAttribute(
            "mode",
            documentId == null
                ? "create"
                : "edit"
        );
        model.addAttribute(
            "isEdit",
            documentId != null
        );
        model.addAttribute("activeMenu", "sale");
        model.addAttribute("pageTitle", "판매 입력");

        return "sale/form";
    }

    // 거래명세서 화면
    @GetMapping("/statement")
    public String statement(Model model) {
        log.info("거래명세서 조회 요청");

        model.addAttribute(
            "form",
            Collections.singletonMap(
                "lines",
                Collections.emptyList()
            )
        );
        model.addAttribute(
            "statementTotals",
            Collections.emptyMap()
        );
        model.addAttribute("activeMenu", "sale");
        model.addAttribute(
            "pageTitle",
            "거래명세서"
        );

        return "sale/statement";
    }

    // 판매 저장
    @PostMapping("/save")
    public String save(
            SaleVO sale,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes rttr) {

        log.info("판매 저장 요청");

        boolean editing =
            sale.getSaleId() != null;

        if (bindingResult.hasErrors()) {
            rttr.addFlashAttribute(
                "errorMessage",
                "품목의 수량과 단가 등 입력값을 확인해 주세요."
            );
        } else {
            try {
                service.save(sale);
            } catch (IllegalArgumentException e) {
                rttr.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
                );
            } catch (DataAccessException e) {
                log.error("판매 저장 실패", e);

                rttr.addFlashAttribute(
                    "errorMessage",
                    "판매를 저장하지 못했습니다. 입력 내용을 확인해 주세요."
                );
            }
        }

        return PostRedirects.afterDocumentSave(
            "/sale",
            editing,
            request
        );
    }

    // 판매 삭제 미구현
    @PostMapping("/delete")
    public String delete(
            HttpServletResponse response,
            Model model) {

        log.info("판매 삭제 요청");

        return ViewModels.notImplemented(
            response,
            model
        );
    }

    // 판매 상태 변경 미구현
    @PostMapping("/change-status")
    public String changeStatus(
            HttpServletResponse response,
            Model model) {

        log.info("판매 진행 상태 변경 요청");

        return ViewModels.notImplemented(
            response,
            model
        );
    }
}
