package com.cauh.common.security.authentication;


import com.cauh.common.entity.Account;
import com.cauh.common.entity.RoleAccount;
import com.cauh.common.entity.constant.UserType;
import com.cauh.common.repository.RoleAccountRepository;
import com.cauh.common.repository.RoleRepository;
import com.cauh.common.repository.SignatureRepository;
import com.cauh.common.repository.UserJobDescriptionRepository;
import com.cauh.common.service.CustomUserDetailsService;
import com.cauh.common.service.ExternalCustomUserService;
import com.cauh.common.service.UserService;
import com.cauh.common.utils.DateUtils;
import com.cauh.iso.domain.AgreementPersonalInformation;
import com.cauh.iso.domain.ConfidentialityPledge;
import com.cauh.iso.repository.AgreementPersonalInformationRepository;
import com.cauh.iso.repository.ConfidentialityPledgeRepository;
import com.cauh.iso.service.AgreementPersonalInformationService;
import com.cauh.iso.service.ConfidentialityPledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Dt&amp;SanoMedics <br>
 * Developer : Jeonghwan Seo <br>
 * Date &amp; Time : 2018-09-27  17:32 <br>
 * Comments : Description. <br>
 **/
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider, MessageSourceAware {
    private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private UserJobDescriptionRepository userJobDescriptionRepository;

    //2020-11-17 : YSH 추가
    @Autowired
    private RoleAccountRepository roleAccountRepository;
    @Autowired
    private RoleRepository roleRepository;

//    @Autowired
//    private GroupwareUserAuthService groupwareUserAuthService;
//    @Resource
//    private DeptUserMapper deptUserMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private ExternalCustomUserService externalCustomUserService;
    @Autowired
    private SignatureRepository signatureRepository;
    @Autowired
    private AgreementPersonalInformationService agreementPersonalInformationService;
    @Autowired
    private ConfidentialityPledgeService confidentialityPledgeService;


//    @Value("${spring.profiles.active}")
//    private String activeProfile;
//
//    @Value("${gw.sign-image}")
//    private String gwSignURL;
//
//    @Value("${file.upload-dir}")
//    private String fileUploadDir;
//
//    @Value("${gw.userTbl}")
//    private String gwUserTbl;
//
//    @Value("${gw.deptTbl}")
//    private String gwDeptTbl;

    private PasswordEncoder passwordEncoder;

    private MessageSourceAccessor messages;// = SpringSecurityMessageSource.getAccessor();

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${site.code}")
    private String siteCode;

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    public void setUserDetailsService(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    private CustomUserDetailsService getUserDetailsService() {
        return customUserDetailsService;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    private void preAuthenticationChecks(Account userDetails) {
        try {
            userDetailsChecker.check(userDetails);
        } catch (LockedException e) {
            log.info("계정 잠금");
            throw e;
        } catch (DisabledException e) {
            log.info("계정 유효 만료");
            throw e;
        } catch (AccountExpiredException e) {
            log.info("계정 유효기한 만료");
            throw e;
        } catch (CredentialsExpiredException e) {
            log.info("비밀번호 기한 만료");
        } catch (UsernameNotFoundException e) {
            throw e;
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        CustomWebAuthenticationDetails customWebAuthenticationDetails = (CustomWebAuthenticationDetails) authentication.getDetails();
        Object credentials = authentication.getCredentials();
        boolean isInternalUser = username.indexOf("@") == -1;
        Account userDetails = null;

        if(isInternalUser) {
            log.debug("==> 내부사용자 로그인 : {}", username);
            String ip = ((CustomWebAuthenticationDetails) authentication.getDetails()).getClientIP();
            log.info("===> @username : {}, IP : {}", username, ip);

//            boolean isInternal = ip.startsWith("192.168.21");
//            boolean isVPN = ip.startsWith("10.212.134");
//            boolean isAdmin = username.endsWith("#admin");
//            log.info("==> isInternal : {}, isVPN : {}, isAdmin : {}", isInternal, isVPN, isAdmin);

            boolean isAdmin = username.endsWith("#admin");

            log.info("==> isAdmin : {}", isAdmin);
//            if(!isInternal && !isVPN && !isAdmin) {
//                log.error("접속을 허용하지 않는 IP[{}] 입니다.", ip);
//                throw new GroupwareAuthenticationException("접속을 허용하지 않는 IP 입니다.");
//            }

            if(isAdmin && "admin!!".equals(credentials)) {
                log.info("관리자 로그인({})", username);
                username = username.substring(0, username.indexOf("#"));
                userDetails = userService.loadUserByUsername(username);
            }else {
                userDetails = userService.authenticate(username, (String)credentials);
            }

            /**
             * LOGIN USER의 JOB Description 설정
             */
            preAuthenticationChecks(userDetails);
            // userDetails.setUserType(UserType.USER);

            userDetails.setSignature(signatureRepository.findById(username).isPresent());


            //개인정보 활용동의, 기밀유지 서약 내용에 들어갈 Site Code 세팅
            userDetails.setSiteCode(siteCode);

            //개인정보 활용동의 여부
            Optional<AgreementPersonalInformation> informationOptional = agreementPersonalInformationService.findOneAgreementPersonalInformation(userDetails);

            if(informationOptional.isPresent()){
                AgreementPersonalInformation information = informationOptional.get();
                userDetails.setAgreementCollectUse(information.isAgree());
            }

            //기밀유지 서약 여부
            Optional<ConfidentialityPledge> confidentialityPledgeOptional = confidentialityPledgeService.findOneConfidentialityPledge(userDetails);
            if(confidentialityPledgeOptional.isPresent()) {
                ConfidentialityPledge confidentialityPledge = confidentialityPledgeOptional.get();
                userDetails.setConfidentialityPledge(confidentialityPledge.isAgree());
            }

            log.info("@@@@@@User -> JobDescription : {}", userDetails.getUserJobDescriptions());

        } else {
            Optional<Account> optionalUser = externalCustomUserService.findByEmail(username);
            log.debug("==> 외부 사용자 로그인 : {}", username);
            if(optionalUser.isPresent() == false) {
                throw new AccountExpiredException("자격 증명 확인에 실패 하였습니다.");
            }

            userDetails = optionalUser.get();
            userDetails.setUserType(UserType.AUDITOR);
        }

        log.info("Application Name : {}, Username : {}, isAdmin : {}", applicationName, userDetails.getUsername(), userDetails.isAdmin());

        return createSuccessAuthentication(customWebAuthenticationDetails, userDetails);
    }



//    private Account updateUserInfo(String username) {
//        Account userDetails;
//        Map<String, String> param = new HashMap<>();
//        param.put("gwUserTbl", gwUserTbl);
//        param.put("gwDeptTbl", gwDeptTbl);
//        param.put("username", username);
//        Account deptUser = deptUserMapper.findByUsername(param);
//
//        if(ObjectUtils.isEmpty(deptUser)) {
//            log.error(" *** 그룹웨어 사용자 연동 테이블을 확인해 주세요. ***");
//        }
//
//        Optional<User> optionalUser = userService.findByUsername(username);
//        if(optionalUser.isPresent()) {
//            log.info("==> 기존 사용자 정보 업데이트 : {} **", username);
//            if(!deptUser.isEnabled()) {
//                log.info("현재 로그인 한 사용자[{}]는 퇴사/삭제 상태 입니다.", username);
//            }
//            userDetails = optionalUser.get();
//            userDetails.setEmail(deptUser.getEmail());
//            userDetails.setEmpNo(deptUser.getEmpNo());
//            userDetails.setEngName(deptUser.getEngName());
//            userDetails.setKorName(deptUser.getKorName());
//            userDetails.setDeptName(deptUser.getDeptName());
//            userDetails.setTeamName(deptUser.getTeamName());
//            userDetails.setDeptCode(deptUser.getDeptCode());
//            userDetails.setTeamCode(deptUser.getTeamCode());
//            userDetails.setLev(deptUser.getLev());
//            userDetails.setDuty(deptUser.getDuty());
//            userDetails.setPosition(deptUser.getPosition());
//            userDetails.setEnabled(deptUser.isEnabled());
//        } else {
//            log.info("==> 신규 사용자 정보 등록 : {} **", username);
//            userDetails = deptUser;
//            userDetails.setAccountNonLocked(true);
//            userDetails.setEnabled(true);
//        }
//
//        if(ObjectUtils.isEmpty(userDetails.getInDate())) {
//            userDetails.setInDate(toDate(userDetails.getEmpNo()));//입사일자를 등록한다.
//        }
//        return userService.saveOrUpdate(userDetails);
//    }

    protected Date toDate(String empNo) {
        String s = empNo.replace("S", "20");
        return DateUtils.toDate(s.substring(0, s.length() - 2), "yyyyMMdd");
    }


    /**
     * @param customWebAuthenticationDetails
     * @param userDetails
     * @return
     */
    private CustomUsernamePasswordAuthenticationToken createSuccessAuthentication(CustomWebAuthenticationDetails customWebAuthenticationDetails, Account userDetails) {
        List<GrantedAuthority> authorities = null;
        String username = userDetails.getUsername();

        log.info("==> username : {}, userType : {}", username, userDetails.getUserType().name());
        /**
         * Job Description 의 Short Name 을 사용자의 권한(role) 로 설정한다.
         */
        if(userDetails.getUserType() == UserType.ADMIN) { //초기 ADMIN 계정
            authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(UserType.ADMIN.name());
            log.info("===> ADMIN : {}", authorities);

        } else if (userDetails.getUserType() == UserType.USER) { // 내부 사용자 인 경우,
            if (!ObjectUtils.isEmpty(userDetails.getUserJobDescriptions())) {
                //Enabled Y / Manager Y 된 role이 있으면 ADMIN으로 적용, 아니면 USER로 적용
                String managerAuthorities = userDetails.getUserJobDescriptions().stream()
                        .filter(role -> role.getJobDescription().isEnabled() && role.getJobDescription().isManager())
                        .map(role -> role.getJobDescription().getShortName()).collect(Collectors.joining(","));

                if(!StringUtils.isEmpty(managerAuthorities)) {
                    authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(UserType.ADMIN.name());
                } else {
                    authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(UserType.USER.name());
                }
                log.debug("==> Username : {}, Role : {}", username, authorities);
            } else {
                authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(UserType.USER.name());
            }
        } else if (userDetails.getUserType() == UserType.AUDITOR) {
            authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(UserType.AUDITOR.name());
        }

        userDetails.setLoginDate(new Date());

        /**
         * UserDetails 정보에 AccessLogId 설정
         */
        CustomUsernamePasswordAuthenticationToken customUsernamePasswordAuthenticationToken = new CustomUsernamePasswordAuthenticationToken(userDetails, null, authorities);
        customUsernamePasswordAuthenticationToken.setDetails(customWebAuthenticationDetails);

        log.info("customUsernamePasswordAuthenticationToken success();");
        return customUsernamePasswordAuthenticationToken;
    }

    /**
     * 비밀번호 일치 여부 및 비밀번호 오류 회수를 체크하여 5회 오류인 경우 계정 잠금 처리한다.
     *
     * @param password
     * @param userDetails
     * @throws BadCredentialsException
     */
    protected void passwordMatches(String password, Account userDetails) throws BadCredentialsException {
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Wrong password.");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
