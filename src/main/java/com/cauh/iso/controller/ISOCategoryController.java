package com.cauh.iso.controller;

import com.cauh.iso.admin.validator.ISOCategoryValidator;
import com.cauh.iso.domain.Category;
import com.cauh.iso.domain.ISOCategory;
import com.cauh.iso.service.DocumentService;
import com.cauh.iso.service.ISOCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"ISOCategory", "CategoryList"})
@Slf4j
public class ISOCategoryController {
    private final ISOCategoryService categoryService;
    private final ISOCategoryValidator categoryValidator;
    //private final DocumentService documentService;

    @GetMapping("/iso/category")
    public String CategoryList(@RequestParam(value = "action", defaultValue = "list") String action,
                               @RequestParam(value = "id", required = false) String id,
                               Model model) {

        List<ISOCategory> CategoryList = categoryService.getCategoryList();
        model.addAttribute("CategoryList", CategoryList);

        if("new".equals(action)) {
            ISOCategory Category = new ISOCategory();
            model.addAttribute("Category", Category);
        } else if ("edit".equals(action)) {
            //long count = categoryService.countByCategoryId(id);
            ISOCategory Category = categoryService.findById(id);
            //Category.setReadonly(count > 0);
            model.addAttribute("Category", Category);
        }

        model.addAttribute("action", action);
        model.addAttribute("id", id);

        return "iso/category/list";
    }

    @PostMapping("/iso/category")
    public String editCategory(@RequestParam(value = "action", defaultValue = "list") String action,
                               @RequestParam(value = "id", required = false) String id,
                               @ModelAttribute("Category") ISOCategory Category,
                               BindingResult result,
                               SessionStatus status, Model model, RedirectAttributes attributes) {
        categoryValidator.validate(Category, result);

        if(result.hasErrors()) {
            model.addAttribute("id", id);
            model.addAttribute("action", action);
            return "iso/category/list";
        }
        categoryService.save(Category);
        status.setComplete();
        attributes.addFlashAttribute("message", "?????? ???????????????.");
        return "redirect:/iso/category";
    }

    @DeleteMapping("/iso/category")
    public String removeCategory(@RequestParam("id") String id, RedirectAttributes attributes) {
        //TODO ????????? ?????? ????????? ??????????????? ????????? ?????????????????? ???????????????
//        long categoryCnt = documentService.countByCategoryId(id);
//
//        if(categoryCnt > 0) {
//            attributes.addFlashAttribute("messageType", "danger");
//            attributes.addFlashAttribute("message", "?????? ?????? :: ?????? ??????????????? ???????????? ????????? ????????????.");
//            return "redirect:/admin/sop/category";
//        }

        ISOCategory category = categoryService.findById(id);

        if(ObjectUtils.isEmpty(category)) {
            attributes.addFlashAttribute("messageType", "danger");
            attributes.addFlashAttribute("message", "??????????????? Category??? ???????????? ????????? ?????? ?????????????????????.");
        } else {
            category.setDeleted(true);
            ISOCategory savedCategory = categoryService.save(category);
            log.debug("?????? ?????? Category : {}", savedCategory);

            attributes.addFlashAttribute("message", "[" + category.getShortName() + "] ??????????????? ?????????????????????.");
        }
        return "redirect:/iso/category";
    }

}
