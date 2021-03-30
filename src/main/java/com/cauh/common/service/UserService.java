package com.cauh.common.service;


import com.cauh.common.entity.Account;
import com.cauh.common.security.authentication.InternalAccountAuthenticationException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.TreeMap;

/**
 * Created by Dt&amp;SanoMedics <br>
 * Developer : Jeonghwan Seo <br>
 * Date &amp; Time : 2018-09-27  16:10 <br>
 * Comments : Description. <br>
 **/
public interface UserService {
    Account loadUserByUsername(String username) throws UsernameNotFoundException;

    Account authenticate(String username, String password) throws AuthenticationException;

    Optional<Account> findUserByEmail(String usernameOrEmail);

    Account saveOrUpdate(Account user);

    Optional<Account> findByUsername(String username);
    
    //회원가입 신청 시 절차
    Account signUpRequest(Account account);
    //회원가입 수락
    Account signUpAccept(Account account, Account manager);
    //회원가입 거절
    Account signUpReject(Account account);

    void signUpMailSend(Account account);

    void userPasswordReset(Account account);
    void agreementCheck();
    void refresh();
    //Login Failure Handler Method
    void countFailure(String username);
    void lockedUser(String username);

    Integer checkFailureCount(String username);

    TreeMap<String, String> getUserMap();
}
