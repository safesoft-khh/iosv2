package com.cauh.common.repository;

import com.cauh.common.entity.Account;
import com.cauh.common.entity.Department;
import com.cauh.common.entity.UserJobDescription;
import com.cauh.common.entity.constant.UserStatus;
import com.cauh.common.entity.constant.UserType;
import com.cauh.iso.domain.Notice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends PagingAndSortingRepository<Account, Integer>, QuerydslPredicateExecutor<Account>, RevisionRepository<Account, Integer, Long> {

    List<Account> findAll();

    Optional<Account> findByUsername(@Param("username") String username);

    Optional<Account> findByUsernameAndEmail(String username, String email);

    List<Account> findAllByUserTypeAndUserStatusOrderByNameAsc(UserType userType, UserStatus status);

    //WHERE enabled = ? and user_status in (?, ?)
    List<Account> findAllByEnabledAndUserStatusIn(boolean enabled, List<UserStatus> userStatus);

    List<Account> findAllByDepartment(Department department);
    Integer countAllByDepartment(Department department);

    long countByUserStatus(UserStatus userStatus);

//    Page<Account> findAllByUserTypeAndEnabled(UserType userType, boolean enabled, Pageable pageable);
}
