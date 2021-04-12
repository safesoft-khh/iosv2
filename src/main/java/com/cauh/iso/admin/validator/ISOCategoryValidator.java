package com.cauh.iso.admin.validator;

import com.cauh.iso.domain.ISOCategory;
import com.cauh.iso.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class ISOCategoryValidator implements Validator {
    private final CategoryService categoryService;

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }

    @Override
    public void validate(Object o, Errors errors) {
        ISOCategory Category = (ISOCategory)o;

        if(StringUtils.isEmpty(Category.getShortName())) {
            errors.rejectValue("shortName", "message.required", "필수 입력 항목입니다.");
        } else if (StringUtils.isEmpty(Category.getId()) && categoryService.findByShortName(Category.getShortName()).isPresent()) {
            errors.rejectValue("shortName", "message.required", "이미 사용중 입니다.");
        }

        if(StringUtils.isEmpty(Category.getName())) {
            errors.rejectValue("name", "message.required", "필수 입력 항목입니다.");
        }
    }
}
