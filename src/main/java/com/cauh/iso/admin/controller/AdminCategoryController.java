package com.cauh.iso.admin.controller;

import com.cauh.iso.domain.Category;
import com.cauh.iso.service.CategoryService;
import com.cauh.iso.service.DocumentService;
import com.cauh.iso.validator.CategoryValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"category"})
public class AdminCategoryController {
    private final CategoryService categoryService;
    private final CategoryValidator categoryValidator;
    private final DocumentService documentService;

    @GetMapping("/admin/sop/category")
    public String categoryList(@RequestParam(value = "action", defaultValue = "list") String action,
                               @RequestParam(value = "id", required = false) String id, Model model) {
        List<Category> categoryList = categoryService.getCategoryList();
        model.addAttribute("categoryList", categoryList);

        if("new".equals(action)) {
            Category Category = new Category();
            model.addAttribute("category", Category);
        } else if ("edit".equals(action)) {
            long count = documentService.countByCategoryId(id);
            Category Category = categoryService.findById(id);
            Category.setReadonly(count > 0);
            model.addAttribute("category", Category);
        }

        model.addAttribute("action", action);
        model.addAttribute("id", id);

        return "admin/sop/category/list";
    }

    @PostMapping("/admin/sop/category")
    public String editCategory(@RequestParam(value = "action", defaultValue = "list") String action,
                               @RequestParam(value = "id", required = false) String id,
                               @ModelAttribute("category") Category Category,
                               BindingResult result,
                               SessionStatus status, Model model, RedirectAttributes attributes) {
        categoryValidator.validate(Category, result);

        if(result.hasErrors()) {
            model.addAttribute("action", action);
            return "admin/sop/category/list";
        }

        categoryService.save(Category);
        status.setComplete();
        attributes.addFlashAttribute("message", "저장 되었습니다.");
        return "redirect:/admin/sop/category";
    }
}
