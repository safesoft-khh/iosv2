package com.dtnsm.esop.validator;

import com.dtnsm.esop.domain.Category;
import com.dtnsm.esop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class CategoryValidator implements Validator {
    private final CategoryService categoryService;

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }

    @Override
    public void validate(Object o, Errors errors) {
        Category category = (Category)o;

        if(StringUtils.isEmpty(category.getShortName())) {
            errors.rejectValue("shortName", "message.required", "필수 입력 항목입니다.");
        } else if (StringUtils.isEmpty(category.getId()) && categoryService.findByShortName(category.getShortName()).isPresent()) {
            errors.rejectValue("shortName", "message.required", "이미 사용중 입니다.");
        }

        if(StringUtils.isEmpty(category.getName())) {
            errors.rejectValue("name", "message.required", "필수 입력 항목입니다.");
        }
    }
}
