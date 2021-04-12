package com.cauh.iso.repository;

import com.cauh.iso.domain.ISOCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ISOCategoryRepository extends JpaRepository<ISOCategory, String>, QuerydslPredicateExecutor<ISOCategory> {

}
