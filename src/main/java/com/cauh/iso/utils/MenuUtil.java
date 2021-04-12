package com.cauh.iso.utils;

import com.cauh.iso.domain.ISOCategory;
import com.cauh.iso.service.ISOCategoryService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MenuUtil {
    private static ISOCategoryService isoCategoryService;

    private MenuUtil(ISOCategoryService isoCategoryService) {
        this.isoCategoryService = isoCategoryService;
    }

    public static List<ISOCategory> getISOCategory() {
        return isoCategoryService.getCategoryList();
    }
}
