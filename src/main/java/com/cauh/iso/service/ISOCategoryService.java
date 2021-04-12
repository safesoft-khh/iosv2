package com.cauh.iso.service;


import com.cauh.iso.domain.ISOCategory;
import com.cauh.iso.domain.QISOCategory;
import com.cauh.iso.repository.ISOCategoryRepository;
import com.querydsl.core.BooleanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ISOCategoryService {

    @Resource
    private ISOCategoryRepository isoCategoryRepository;

    public ISOCategory save(ISOCategory Category) {
        return isoCategoryRepository.save(Category);
    }

    public ISOCategory findById(String id) {
        Optional<ISOCategory> optionalCategory = isoCategoryRepository.findById(id);
        return optionalCategory.isPresent() ? optionalCategory.get() : null;
    }

    public Optional<ISOCategory> findByShortName(String shortName) {
        QISOCategory qCategory = QISOCategory.iSOCategory;
        return isoCategoryRepository.findOne(qCategory.shortName.eq(shortName));
    }

    public List<ISOCategory> getCategoryList() {
        QISOCategory qCategory = QISOCategory.iSOCategory;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qCategory.deleted.eq(false));

        return StreamSupport.stream(isoCategoryRepository.findAll(builder, Sort.by(Sort.Direction.ASC, "shortName")).spliterator(), false)
                .collect(Collectors.toList());
    }

    public TreeMap<String, String> categoryMap() {
        TreeMap<String, String> categoryMap = new TreeMap<>();
        categoryMap.putAll(getCategoryList().stream().filter(c -> !c.isDeleted())
                .collect(Collectors.toMap(c -> c.getShortName(), c-> "[" + c.getShortName() + "] " + c.getName())));
        return categoryMap;
    }
}
