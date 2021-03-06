package com.cauh.iso.repository;

import com.cauh.iso.domain.ISOOfflineTrainingAttendee;
import com.cauh.iso.domain.ISOOfflineTrainingDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ISOOfflineTrainingDocumentRepository extends JpaRepository<ISOOfflineTrainingDocument, Integer>, QuerydslPredicateExecutor<ISOOfflineTrainingDocument> {

}
