package com.cauh.common.service;

import com.cauh.common.entity.*;
import com.cauh.common.entity.constant.JobDescriptionStatus;
import com.cauh.common.entity.constant.RoleStatus;
import com.cauh.common.entity.constant.UserStatus;
import com.cauh.common.entity.constant.UserType;
import com.cauh.common.repository.DepartmentRepository;
import com.cauh.common.repository.UserJobDescriptionChangeLogRepository;
import com.cauh.common.repository.UserJobDescriptionRepository;
import com.cauh.common.repository.UserRepository;
import com.cauh.common.security.authentication.InternalAccountAuthenticationException;
import com.cauh.common.security.authentication.SignUpRequestedAccountException;
import com.cauh.iso.domain.ConfidentialityPledge;
import com.cauh.iso.domain.NonDisclosureAgreement;
import com.cauh.iso.utils.DateUtils;
import com.cauh.iso.component.CurrentUserComponent;
import com.cauh.iso.domain.AgreementPersonalInformation;
import com.cauh.iso.domain.Mail;
import com.cauh.iso.repository.AgreementPersonalInformationRepository;
import com.cauh.iso.repository.ConfidentialityPledgeRepository;
import com.cauh.iso.repository.NonDisclosureAgreementRepository;
import com.cauh.iso.service.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by Dt&amp;SanoMedics <br>
 * Developer : Jeonghwan Seo <br>
 * Date &amp; Time : 2018-09-27  16:11 <br>
 * Comments : Description. <br>
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserJobDescriptionRepository userJobDescriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    //현재 유저정보를 가지고있는 Component
    private final CurrentUserComponent currentUserComponent;
    private final UserJobDescriptionChangeLogRepository userJobDescriptionChangeLogRepository;
    private final JobDescriptionService jobDescriptionService;
    private final MailService mailService;
    private final AgreementPersonalInformationRepository agreementPersonalInformationRepository;
    private final ConfidentialityPledgeRepository confidentialityPledgeRepository;
    private final NonDisclosureAgreementRepository nonDisclosureAgreementRepository;

//    private final DeptUserMapper deptUserMapper;
//
//    @Value("${gw.userTbl}")
//    private String gwUserTbl;
//
//    @Value("${gw.deptTbl}")
//    private String gwDeptTbl;

    @Override
    public Account loadUserByUsername(String username) throws UsernameNotFoundException{
//        User user = authenticationMapper.loadUserByUsername(username);
        Optional<Account> optionalUser = userRepository.findByUsername(username);
        if(optionalUser.isPresent()) {
            return optionalUser.get();
        }
        throw new UsernameNotFoundException("[" + username + "] Username Not Found.");
    }

    @Override
    public Account authenticate(String username, String password){
        log.info("@UserService : Authenticate");
        Optional<Account> accountOptional = userRepository.findByUsername(username);

        if(accountOptional.isPresent())
        {
            log.info("@Authenticate ID 확인");
            //비밀번호 Matching이 되지 않거나, Account 계정이 없는 경우 실패
            Account account = accountOptional.get();

            if(account.getUserStatus() == UserStatus.ACTIVE) {
                account.getPassword();
                if(passwordEncoder.matches(password, account.getPassword())) {
                    return account;
                }
                throw new InternalAccountAuthenticationException("내부사용자 로그인에 실패하였습니다.");
            } else if (account.getUserStatus() == UserStatus.SIGNUP_REQUEST) {
                throw new SignUpRequestedAccountException("가입 신청중인 사용자입니다.");
            } else if (ObjectUtils.isEmpty(account.getUserStatus())){
                throw new InternalAccountAuthenticationException("내부사용자 로그인에 실패하였습니다.");
            }
        }
        throw new UsernameNotFoundException("[" + username + "] Username Not Found.");
    }

    @Override
    public Optional<Account> findUserByEmail(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail);
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Account saveOrUpdate(Account user) {
        return userRepository.save(user);
    }

    @Override
    public void userPasswordReset(Account account) {
        //계정 비밀번호 Setting 구간
        String rdPassword = getRandomPassword(10);
        account.setPassword(passwordEncoder.encode(rdPassword));
        LocalDate pwDueDate = LocalDate.now().minusDays(1); //비밀번호 기한 초과되게 만들기. (변경을 위한 동작)
        account.setCredentialsExpiredDate(Date.from(pwDueDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        userRepository.save(account);

        //계정 Mail 전송 구간.
        HashMap<String, Object> model = new HashMap<>();
        model.put("message", "임시 비밀번호 안내");
        model.put("username", account.getUsername());
        model.put("password", rdPassword);

        Mail mail = Mail.builder()
                .to(new String[]{account.getEmail()})
                .subject(String.format("[ISO-MS/System] 사용자 임시 비밀번호 안내"))
                .model(model)
                .templateName("user-password-reset")
                .build();

        mailService.sendMail(mail);
    }

    @Override
    public Account signUpRequest(Account account) {
        //userStatus를 통해 현재 유저 상태 설정 (SIGNUP_REQUEST)
        account.setUserType(UserType.USER);
        account.setAccountNonLocked(true);
        account.setEnabled(true);
        account.setUserStatus(UserStatus.SIGNUP_REQUEST);

        //입력된 비밀번호 암호화
        account.setPassword(passwordEncoder.encode(account.getPassword()));

        //가입 시 계정 유효기한을 설정(가입시점 + D-14)
        LocalDate localDate = LocalDate.now().plusDays(14);
        Date DDay_14 = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        account.setAccountExpiredDate(DDay_14);

        //부서입력
        if(!ObjectUtils.isEmpty(account.getDepartment())) {
            Department department = account.getDepartment();

            //상위 부서가 존재할 경우
            if(!ObjectUtils.isEmpty(department.getParentDepartment())) {
                account.setDeptName(department.getParentDepartment().getName());
                account.setTeamName(department.getName());
            } else { //상위 부서가 없을 경우
                account.setDeptName(department.getName());
                account.setTeamName(null);
            }
        }

        if(!ObjectUtils.isEmpty(account.getJdIds())){
            //selectedIds 목록 추가
            List<String> selectedIds = Arrays.asList(account.getJdIds());
            for(String id : selectedIds) {
                UserJobDescription userJobDescription = new UserJobDescription();
                userJobDescription.setJobDescription(JobDescription.builder().id(Integer.parseInt(id)).build());
                userJobDescription.setUser(account);
                userJobDescription.setStatus(JobDescriptionStatus.APPROVED);

                userJobDescriptionRepository.save(userJobDescription);
            }
        }

        return account;
    }

    @Override
    public Account signUpAccept(Account account, Account manager) {
        //가입 수락 - 가입 날짜 설정
        //TODO 한경훈 추가 회원가입시 필수 입력 항목으로 빠짐
        //account.setIndate(new Date());
        //가입 수락 - 계정 기한 설정
        LocalDate accountExpiredDate = LocalDate.of(9999, 12, 31);
        account.setAccountExpiredDate(Date.from(accountExpiredDate.atStartOfDay(ZoneId.systemDefault()).toInstant())); //9999-12-31 설정
        //가입 수락 - 비밀번호 기한 설정
        LocalDate credentialExpiredDate = LocalDate.now().plusDays(90); // + 90일
        account.setCredentialsExpiredDate(Date.from(credentialExpiredDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        account.setUserStatus(UserStatus.ACTIVE);
        account.setEnabled(true);

        //부서입력
        if(!ObjectUtils.isEmpty(account.getDepartment())) {
            Department department = account.getDepartment();
            //상위 부서가 존재할 경우
            if(!ObjectUtils.isEmpty(department.getParentDepartment())) {
                account.setDeptName(department.getParentDepartment().getName());
                account.setTeamName(department.getName());
            } else { //상위 부서가 없을 경우
                account.setDeptName(department.getName());
                account.setTeamName(null);
            }
        }

        //JD 재입력
        if(!ObjectUtils.isEmpty(account.getJdIds())){
            List<String> selectedIds = Arrays.asList(account.getJdIds());
            List<UserJobDescription> newUserJobDescriptions = new ArrayList<>();
            List<UserJobDescription> userJobDescriptions = account.getUserJobDescriptions();

            //변경 전
            String prevRole = userJobDescriptions.stream().map(d -> d.getJobDescription().getShortName()).collect(Collectors.joining(","));
            log.info("Prev : {}", prevRole);

            List<String> currentJdIds = userJobDescriptions.stream().map(jd -> Integer.toString(jd.getJobDescription().getId())).collect(Collectors.toList());
            LocalDate now = LocalDate.now();

            //새로이 선택된 값에 중복되거나 기존에 겹치는 값 설정.
            for(UserJobDescription userJobDescription : userJobDescriptions){
                //사라진 값 -> userDescription 삭제
                if(!selectedIds.contains(Integer.toString(userJobDescription.getJobDescription().getId()))){
                    userJobDescriptionRepository.delete(userJobDescription);
                }else { //기존에 유지되는 값 - AssginDate 설정
                    userJobDescription.setAssignDate(Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    UserJobDescription savedUserJobDescription = userJobDescriptionRepository.save(userJobDescription);
                    newUserJobDescriptions.add(savedUserJobDescription);
                }
            }

            List<String> nextRoleList = new ArrayList<>();

            //새로 선택된 UserJobDescription 선택
            for(String id : selectedIds) {
                if(!currentJdIds.contains(id)){ //기존 JDIds에 없는 Case 생성
                    UserJobDescription userJobDescription = new UserJobDescription();
                    userJobDescription.setJobDescription(JobDescription.builder().id(Integer.parseInt(id)).build());
                    userJobDescription.setUser(account);
                    userJobDescription.setStatus(JobDescriptionStatus.APPROVED);
                    userJobDescription.setAssignDate(Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    UserJobDescription savedUserJobDescription = userJobDescriptionRepository.save(userJobDescription);
                    newUserJobDescriptions.add(savedUserJobDescription);
                }

                nextRoleList.add(jobDescriptionService.findById(Integer.parseInt(id)).getShortName());
            }

            //변경 후
            String nextRole = nextRoleList.stream().map(d -> d).collect(Collectors.joining(","));
            log.info("Next : {}", nextRole);

            UserJobDescriptionChangeLog userJobDescriptionChangeLog = UserJobDescriptionChangeLog.builder()
                    .requester(account)
                    .manager(manager)
                    .requestDate(new Date())
                    .assignDate(Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .prevJobDescription(prevRole)
                    .nextJobDescription(nextRole)
                    .reason("신규 가입")
                    .roleStatus(RoleStatus.ACCEPTED)
                    .build();
            userJobDescriptionChangeLogRepository.save(userJobDescriptionChangeLog);

            //유저 가입 수락 후 현재 유저 목록 갱신.
            currentUserComponent.updateCurrentUserList();
        }

        //Account 저장 시, UserJobDescription 저장데이터 제거
        account.setUserJobDescriptions(null);
        return account;
    }


    public Account signUpReject(Account account) {
        //계정 유효기간 만료로 처리 (현재 시간 입력)
        //계정 상태 INACTIVE 지정 / Enabled 변수 false로 설정.
        account.setAccountExpiredDate(new Date());
        account.setUserStatus(UserStatus.INACTIVE);
        account.setEnabled(false);

        return account;
    }

    public void signUpMailSend(Account account) {
        HashMap<String, Object> model = new HashMap<>();

        if (account.getUserStatus() == UserStatus.ACTIVE) { //Accept
            model.put("title", "회원가입 수락 안내");
            model.put("content", "귀하의 회원가입 신청이 수락되었습니다.");
        } else if (account.getUserStatus() == UserStatus.INACTIVE) { //Reject
            model.put("message", "회원가입 거절 안내");
            model.put("username", "귀하의 회원가입 신청이 거절되었습니다.");
        }

        Mail mail = Mail.builder()
                .to(new String[]{account.getEmail()})
                .subject(String.format("[ISO-MS] 회원가입 신청 결과 안내"))
                .model(model)
                .templateName("user-signup-request")
                .build();

        mailService.sendMail(mail);
    }

    @Override
    public void agreementCheck() {
        //로그인 시, 자동 유효기한 처리로 인해 사용안함.
//        List<Account> accountList = userRepository.findAll();
//        //계정 사용 기한 만료 시 계정 비활성화 처리
//        accountList.stream().filter(account -> !DateUtils.isFutureDate(account.getAccountExpiredDate()))
//        .forEach( account -> {
//                account.setEnabled(false);
//                account.setUserStatus(UserStatus.INACTIVE);
//                Account savedUser = userRepository.save(account);
//                log.debug("@@비활성화된 사용자 : {}", savedUser);
//        });

//        //개인정보 활용동의가 처리된 내용 중, 5년이 경과된 것만 비동의 처리
//        Iterable<AgreementPersonalInformation> agreementPIs = agreementPersonalInformationRepository.findAll();
//        StreamSupport.stream(agreementPIs.spliterator(), false).filter(a -> a.isAgree())
//                .forEach(a -> {
//                    //CASE 1. 수정된 날짜가 존재하면 (수정 날짜 기준)
//                    if (!ObjectUtils.isEmpty(a.getLastModifiedDate())) {
//                        //마지막 수정일자로부터 5년 뒤
//                        Date dueDate = DateUtils.addDay(a.getLastModifiedDate(), 365*5);
//                        if(!DateUtils.isFutureDate(dueDate)) {
//                            a.setAgree(false);
//                            // Log 기록
//                            log.debug("User Agreement 5년 경과로 인한 거부 처리 : {}", ObjectUtils.isEmpty(a.getInternalUser())?a.getExternalCustomer():a.getInternalUser());
//                        }
//                    }
//                    //CASE 2. 수정된 날짜가 없으면, (생성 날짜 기준)
//                    else {
//                        //생성일자로부터 5년 뒤
//                        Date dueDate = DateUtils.addDay(a.getCreatedDate(), 365*5);
//                        if(!DateUtils.isFutureDate(dueDate)) {
//                            a.setAgree(false);
//                            // Log 기록
//                            log.debug("User Agreement 5년 경과로 인한 거부 처리 : {}", ObjectUtils.isEmpty(a.getInternalUser())?a.getExternalCustomer():a.getInternalUser());
//                        }
//                    }
//                });
//
//        //비밀 보장 서약 동의가 처리된 내용 중, 5년이 경과된 것만 비동의 처리
//        Iterable<ConfidentialityPledge> agreementCPs = confidentialityPledgeRepository.findAll();
//        StreamSupport.stream(agreementCPs.spliterator(), false).filter(a -> a.isAgree())
//                .forEach(cp -> {
//                    //CASE 1. 수정된 날짜가 존재하면 (수정 날짜 기준)
//                    if (!ObjectUtils.isEmpty(cp.getLastModifiedDate())) {
//                        //마지막 수정일자로부터 5년 뒤
//                        Date dueDate = DateUtils.addDay(cp.getLastModifiedDate(), 365*5);
//                        if(!DateUtils.isFutureDate(dueDate)) {
//                            cp.setAgree(false);
//                            // Log 기록
//                            log.debug("User Agreement 5년 경과로 인한 거부 처리 : {}", cp.getInternalUser());
//                        }
//                    }
//                    //CASE 2. 수정된 날짜가 없으면, (생성 날짜 기준)
//                    else {
//                        //생성일자로부터 5년 뒤
//                        Date dueDate = DateUtils.addDay(cp.getCreatedDate(), 365*5);
//                        if(!DateUtils.isFutureDate(dueDate)) {
//                            cp.setAgree(false);
//                            // Log 기록
//                            log.debug("User Agreement 5년 경과로 인한 거부 처리 : {}", cp.getInternalUser());
//                        }
//                    }
//                });
//
//        //SOP 비공개 동의가 처리된 내용 중, 5년이 경과된 것만 삭제
//        Iterable<NonDisclosureAgreement> agreementNDAs = nonDisclosureAgreementRepository.findAll();
//        StreamSupport.stream(agreementNDAs.spliterator(), false)
//                .forEach(nda -> {
//                    //CASE 1. 수정된 날짜가 존재하면 (수정 날짜 기준)
//                    if (!ObjectUtils.isEmpty(nda.getLastModifiedDate())) {
//                        //마지막 수정일자로부터 5년 뒤
//                        Date dueDate = DateUtils.addDay(nda.getLastModifiedDate(), 365*5);
//                        if(!DateUtils.isFutureDate(dueDate)) {
//                            // Log 기록
//                            log.debug("Non Disclosure 5년 경과로 인한 삭제 처리 : {}", nda.getExternalCustomer());
//                            nonDisclosureAgreementRepository.delete(nda);
//                        }
//                    }
//                    //CASE 2. 수정된 날짜가 없으면, (생성 날짜 기준)
//                    else {
//                        //생성일자로부터 5년 뒤
//                        Date dueDate = DateUtils.addDay(nda.getCreatedDate(), 365*5);
//                        if(!DateUtils.isFutureDate(dueDate)) {
//                            // Log 기록
//                            log.debug("Non Disclosure 5년 경과로 인한 삭제 처리 : {}", nda.getExternalCustomer());
//                            nonDisclosureAgreementRepository.delete(nda);
//                        }
//                    }
//                });
    }


    /**
     * Account Refresh 기능
     */
    @Override
    public void refresh(){
        List<UserStatus> userStatusList = new ArrayList<>();
        userStatusList.add(UserStatus.SIGNUP_REQUEST);
        userStatusList.add(UserStatus.ACTIVE);

        //enabled 가 true이거나 회원가입 신청 / 활성화된 계정 대상으로 refresh 진행.
        List<Account> accountList = userRepository.findAllByEnabledAndUserStatusIn(true, userStatusList);
        for(Account account : accountList) {
            //오늘날짜 기준, 계정 유효기한이 경과한 경우
            if(!DateUtils.isFutureDate(account.getAccountExpiredDate())) {
                log.debug("Account Refrsh=== 계정 유효기한 경과 : {}", account.getUsername());
                account.setUserStatus(UserStatus.INACTIVE);
                account.setEnabled(false);
                userRepository.save(account);
            }
        }

        //User List 업데이트
        currentUserComponent.updateCurrentUserList();
    }

    @Override
    public void countFailure(String username) {
        Optional<Account> account = userRepository.findByUsername(username);
        if(account.isPresent()){
            account.get().setLoginFailCnt(account.get().getLoginFailCnt()+1);
            userRepository.save(account.get());
        }
    }

    @Override
    public void lockedUser(String username) {
        Optional<Account> account = userRepository.findByUsername(username);
        if(account.isPresent()){
            account.get().setAccountNonLocked(false);
            userRepository.save(account.get());
        }
    }

    @Override
    public Integer checkFailureCount(String username) {
        return null;
    }

    protected Date toDate(String empNo) {
        String s = empNo.replace("S", "20");
        return DateUtils.toDate(s.substring(0, s.length() - 2), "yyyyMMdd");
    }

    public static String getRandomPassword(int len) {
        char[] charSet = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        int idx = 0;
        StringBuffer sb = new StringBuffer();
//        System.out.println("charSet.length :::: " + charSet.length);
        for (int i = 0; i < len; i++) {
            idx = (int) (charSet.length * Math.random()); // 36 * 생성된 난수를 Int로 추출 (소숫점제거)
            System.out.println("idx :::: "+idx);
            sb.append(charSet[idx]);
        }

        return sb.toString();
    }

    @Override
    public TreeMap<String, String> getUserMap() {
        //내부 사용자만 가지고 오기.
        QAccount qAccount = QAccount.account;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qAccount.userType.eq(UserType.USER));
        builder.and(qAccount.userStatus.eq(UserStatus.ACTIVE));

        Iterable<Account> users = userRepository.findAll(builder, Sort.by(Sort.Direction.ASC, "deptName", "teamName"));
        Map<String, String> userAscMap = StreamSupport.stream(users.spliterator(), false)
                .collect(Collectors.toMap(u -> Integer.toString(u.getId()), u -> u.getName() + "[" + u.getTeamDept()  + "]"));

        TreeMap<String, String> sortedUserMap = new TreeMap<>();
        sortedUserMap.putAll(userAscMap);

        return sortedUserMap;
    }
}
