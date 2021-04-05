package com.cauh.iso.admin.controller;

import com.cauh.common.entity.*;
import com.cauh.common.entity.constant.RoleStatus;
import com.cauh.common.entity.constant.UserStatus;
import com.cauh.common.entity.constant.UserType;
import com.cauh.common.mapper.DeptUserMapper;
import com.cauh.common.repository.UserJobDescriptionChangeLogRepository;
import com.cauh.common.repository.UserRepository;
import com.cauh.common.security.annotation.CurrentUser;
import com.cauh.common.service.UserService;
import com.cauh.iso.admin.service.DepartmentService;
import com.cauh.iso.domain.AgreementPersonalInformation;
import com.cauh.iso.domain.Mail;
import com.cauh.iso.domain.NonDisclosureAgreement;
import com.cauh.iso.domain.QAgreementPersonalInformation;
import com.cauh.iso.domain.constant.ApprovalStatus;
import com.cauh.iso.domain.report.QExternalCustomer;
import com.cauh.iso.repository.ExternalCustomerRepository;
import com.cauh.iso.service.*;
import com.cauh.iso.validator.UserEditValidator;
import com.cauh.iso.xdocreport.AgreementReportService;
import com.cauh.iso.xdocreport.NonDisclosureAgreementReportService;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@SessionAttributes({"account"})
public class AdminAuthorityController {
    private final AgreementPersonalInformationService agreementPersonalInformationService;
    private final NonDisclosureAgreementService nonDisclosureAgreementService;
    private final PasswordEncoder passwordEncoder;
    //    private final SOPDisclosureRequestFormRepository sopDisclosureRequestFormRepository;
    private final AgreementReportService agreementReportService;
    private final ConfidentialityPledgeService confidentialityPledgeService;
    private final NonDisclosureAgreementReportService nonDisclosureAgreementReportService;
    private final AgreementsWithdrawalService agreementsWithdrawalService;
    private final UserRepository userRepository;
    private final UserEditValidator userEditValidator;
    private final DeptUserMapper deptUserMapper;
    private final UserService userService;
    private final JDService jdService;
    private final DepartmentService departmentService;
    private final ExternalCustomerRepository externalCustomerRepository;
    private final UserJobDescriptionChangeLogRepository userJobDescriptionChangeLogRepository;
    private final UserJobDescriptionChangeLogService userJobDescriptionChangeLogService;

//    @Value("${gw.userTbl}")
//    private String gwUserTbl;
//
//    @Value("${gw.deptTbl}")
//    private String gwDeptTbl;

    @GetMapping("/authority/accounts")
    public String accounts(@PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable, Model model) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QExternalCustomer qExternalCustomer = QExternalCustomer.externalCustomer;
        booleanBuilder.and(qExternalCustomer.sopDisclosureRequestForm.approval.status.eq(ApprovalStatus.approved));

        model.addAttribute("account", externalCustomerRepository.findAll(booleanBuilder, pageable));

        return "admin/authority/account";
    }


    @GetMapping("/authority/agreement-to-collect-and-use")
    public String agreement(){
        return "redirect:/admin/authority/agreement-to-collect-and-use/internal";
    }

    @GetMapping( "/authority/agreement-to-collect-and-use/{type}")
    public String agreement(@PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable,
                            @PathVariable("type") String type, Model model) {
        QAgreementPersonalInformation qAgreementPersonalInformation = QAgreementPersonalInformation.agreementPersonalInformation;
        BooleanBuilder builder = new BooleanBuilder();

        //CASE 1. Internal User에 대한 개인정보 활용동의
        if(!StringUtils.isEmpty(type) && type.equals("internal")) {
            builder.and(qAgreementPersonalInformation.internalUser.isNotNull());
            builder.and(qAgreementPersonalInformation.externalCustomer.isNull());
            model.addAttribute("agreement", agreementPersonalInformationService.findAll(builder, pageable));
            return "admin/authority/agreement-internal";
        }
        //CASE 2. External User에 대한 개인정보 활용동의
        else if(!StringUtils.isEmpty(type) && type.equals("external")){
            builder.and(qAgreementPersonalInformation.internalUser.isNull());
            builder.and(qAgreementPersonalInformation.externalCustomer.isNotNull());
            model.addAttribute("agreement", agreementPersonalInformationService.findAll(builder, pageable));
            return "admin/authority/agreement-external";
        }

        return "redirect:/admin/authority/agreement-to-collect-and-use/internal";
    }

    @GetMapping("/authority/agreement-to-collect-and-use/{id}/print")
    public void agreement(@PathVariable("id") Integer id, HttpServletResponse response) {
        response.setContentType("application/octet-stream");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Agreement to Collect and Use Personal Information.pdf");
        AgreementPersonalInformation agreement = agreementPersonalInformationService.findById(id).get();
        agreementReportService.generateReport(agreement, response);
    }


    @GetMapping("/authority/confidentiality-pledge")
    public String confidentialPledge(@PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable, Model model){
        model.addAttribute("confidentialpledge", confidentialityPledgeService.findAll(pageable));

        return "admin/authority/confidentiality-pledge";
    }

    @GetMapping("/authority/non-disclosure-agreement-for-sop")
    public String nonDisclosureSOP(@PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable, Model model) {
        model.addAttribute("agreement", nonDisclosureAgreementService.findAll(pageable));
        return "admin/authority/non-disclosure-agreement";
    }

    @GetMapping("/authority/non-disclosure-agreement-for-sop/{id}/print")
    public void nonDisclosureSOP(@PathVariable("id") Integer id, HttpServletResponse response) {
        response.setContentType("application/octet-stream");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Non-Disclosure Agreement for SOP.pdf");
        NonDisclosureAgreement agreement = nonDisclosureAgreementService.findById(id).get();
        nonDisclosureAgreementReportService.generateReport(agreement, response);
    }

    @GetMapping("/authority/agreements-withdrawal")
    public String agreementsWithdrawal(@PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable, Model model) {

        model.addAttribute("withdrawal", agreementsWithdrawalService.findAll(pageable));

        return "admin/authority/agreements-withdrawal";
    }

    @GetMapping("/authority/users")
    public String users(@PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable,
                        @RequestParam(value = "deptCode", required = false) String deptCode,
                        @RequestParam(value = "teamCode", required = false) String teamCode,
                        @RequestParam(value = "name", required = false) String name,
                        Model model) {
        QUserJobDescriptionChangeLog qUserJobDescriptionChangeLog = QUserJobDescriptionChangeLog.userJobDescriptionChangeLog;;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qUserJobDescriptionChangeLog.roleStatus.eq(RoleStatus.REQUESTED));
        model.addAttribute("roleRequestList", userJobDescriptionChangeLogRepository.findAll(builder, pageable));

        builder = new BooleanBuilder();
        QAccount qUser = QAccount.account;

        if(StringUtils.isEmpty(name) == false) {
            builder.and(qUser.name.like("%" + name + "%"));
        }

        builder.and(qUser.enabled.eq(true));
        builder.and(qUser.userType.eq(UserType.USER));
        
        //TODO 한경훈 수정
        builder.and(qUser.userStatus.in(UserStatus.ACTIVE, UserStatus.SIGNUP_REQUEST));

        model.addAttribute("account", new Account());
        model.addAttribute("departments", departmentService.getParentDepartment());
        model.addAttribute("jobDescriptionMap", jdService.getJDMap());
        model.addAttribute("users", userRepository.findAll(builder, pageable));
        return "admin/authority/users";
    }

    /**
     *
     * @param actionCmd
     * @param id
     * @param attributes
     * @return
     */
    @PostMapping("/authority/users")
    public String userSignUpAction(@CurrentUser Account user,
            @RequestParam(value = "result", required = false ) String actionCmd,
            @RequestParam(value = "department", required = false) Integer departmentId,
            @RequestParam(value = "jdIds", required = false) String[] jdIds,
            @RequestParam("id") Integer id, RedirectAttributes attributes){
        log.info("Action : {}, User id : {}", actionCmd, id);
        Optional<Account> optionalAccount = userRepository.findById(id);

        if(!optionalAccount.isPresent()) {
            attributes.addFlashAttribute("messageType", "danger");
            attributes.addFlashAttribute("message", "User 정보가 잘못되었습니다.");
            return "redirect:/admin/authority/users";
        }
        Account account = optionalAccount.get();

        //2021-03-12 추가
        if(account.getUserStatus() != UserStatus.SIGNUP_REQUEST) {
            attributes.addFlashAttribute("messageType", "danger");
            attributes.addFlashAttribute("message", "가입 신청 중인 사용자가 아닙니다.");
            return "redirect:/admin/authority/users";
        }

        //SignUp Accept / Reject
        if(!StringUtils.isEmpty(actionCmd)) {
            Account savedUser;

            if(actionCmd.equals("accept")) {
                log.debug("동의 : {}", account.getUsername());

                if(!ObjectUtils.isEmpty(departmentId)) {
                    account.setDepartment(departmentService.getDepartmentById(departmentId));
                    log.debug("AC Dept : {}", account.getDepartment());
                }

                if(!ObjectUtils.isEmpty(jdIds)){
                    account.setJdIds(jdIds);
                    log.debug("JD IDs : {}", jdIds);
                }

                //계정 유효기간 설정
                Account acceptUser = userService.signUpAccept(account, user);
                log.info("User's JD : {}", acceptUser.getUserJobDescriptions());

                savedUser = userService.saveOrUpdate(acceptUser);

                String message = "[" + savedUser.getUsername() + "] 가입 요청이 수락되었습니다.";
                attributes.addFlashAttribute("message", message);

            }else if(actionCmd.equals("reject")){
                log.debug("거절 : {}", account.getUsername());
                Account rejectUser = userService.signUpReject(account);
                savedUser = userService.saveOrUpdate(rejectUser);

                String message = "[" + savedUser.getUsername() + "] 가입 요청이 거절되었습니다.";
                attributes.addFlashAttribute("meesageType", "warning");
                attributes.addFlashAttribute("message", message);
            }else {
                attributes.addFlashAttribute("meesageType", "danger");
                attributes.addFlashAttribute("message", "비정상적인 Sign Up Action입니다.");
                return "redirect:/admin/authority/users";
            }

            //유저정보가 저장되었으면 메일 안내
            if(!ObjectUtils.isEmpty(savedUser)) {
                userService.signUpMailSend(savedUser);
            }

        }
        return "redirect:/admin/authority/users";
    }


    /**
     * Role Request 신청 수락 및 거절
     * @param id
     * @param action
     * @param attributes
     * @return
     */
    @PutMapping("/authority/users/role_request")
    @Transactional
    public String roleRequest(@RequestParam("id") Integer id,
                              @RequestParam("action") String action,
                              @CurrentUser Account manager,
                              RedirectAttributes attributes){
        log.debug("==== role Request");
        log.debug("id : {}", id);
        log.debug("action : {}", action);

        Optional<UserJobDescriptionChangeLog> userJobDescriptionChangeLogOptional
                = userJobDescriptionChangeLogRepository.findById(id);

        if(userJobDescriptionChangeLogOptional.isPresent()) {
            UserJobDescriptionChangeLog userJobDescriptionChangeLog = userJobDescriptionChangeLogOptional.get();
            userJobDescriptionChangeLog.setManager(manager); //매니저 설정.
            Account user = userJobDescriptionChangeLog.getRequester();

            //userJobDescriptionChangeLog Status Change
            if(action.equals("accept")) {
                userJobDescriptionChangeLog.setRoleStatus(RoleStatus.ACCEPTED);

                //UserJobDescription working
                String prevJDs = userJobDescriptionChangeLog.getPrevJobDescription();
                String nextJDs = userJobDescriptionChangeLog.getNextJobDescription();
                Date assignDate = userJobDescriptionChangeLog.getAssignDate();

                userJobDescriptionChangeLogService.updateUserJobDescription(user, prevJDs, nextJDs, assignDate);
                attributes.addFlashAttribute("message", "[" + user.getName() + "]의 ROLE 신청이 수락되었습니다.");

            } else if(action.equals("reject")){ //거절 하면 변동 없이 끝
                userJobDescriptionChangeLog.setRoleStatus(RoleStatus.REJECTED);
                user = userJobDescriptionChangeLog.getRequester();

                attributes.addFlashAttribute("messageType", "warning");
                attributes.addFlashAttribute("message", "[" + user.getName() + "]의 ROLE 신청이 반려되었습니다.");
            } else {
                attributes.addFlashAttribute("messageType", "danger");
                attributes.addFlashAttribute("message", "잘못된 동작을 수행하였습니다.");
                return "redirect:/admin/authority/users";
            }

            userJobDescriptionChangeLogRepository.save(userJobDescriptionChangeLog);
        }
        
        return "redirect:/admin/authority/users";
    }

    @PostMapping("/authority/users/pwReset/{id}")
    @Transactional
    public String passwordReset(@PathVariable("id") Integer id, RedirectAttributes attributes){
        Optional<Account> accountOptional = userRepository.findById(id);

        if(!accountOptional.isPresent()){ // 유저 정보가 없으면
            attributes.addFlashAttribute("messageType", "danger");
            attributes.addFlashAttribute("message", "잘못된 유저 정보입니다.");
            return "redirect:/admin/authority/users";
        }
        Account account = accountOptional.get();

        //User Password Reset
        userService.userPasswordReset(account);

        attributes.addFlashAttribute("message", "[" + account.getName() + "]님의 임시 비밀번호가 이메일로 전송되었습니다.");
        return "redirect:/admin/authority/users";
    }

    /**
     * 유저 정보 수정
     * @param id
     * @param model
     * @param attributes
     * @return
     */
    @GetMapping("/authority/users/{id}")
    public String usersEdit(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes) {
        log.info("ID : {}", id);
        Optional<Account> optionalAccount = userRepository.findById(id);
        if(!optionalAccount.isPresent()) {
            attributes.addFlashAttribute("message", "존재하지 않는 유저 정보입니다.");
            return "redirect:/admin/authority/users";
        }
        Account account = optionalAccount.get();

        //User Edit
        model.addAttribute("account", account);
        model.addAttribute("departments", departmentService.getParentDepartment());

        return "admin/authority/edit";
    }

    @PostMapping("/authority/users/{id}")
    @Transactional
    public String usersEditAction(@ModelAttribute("account")Account account, Model model, SessionStatus sessionStatus, RedirectAttributes attributes, BindingResult result) {
        userEditValidator.validate(account, result);
        if(result.hasErrors()) {
            log.debug("User Edit Errors : {}==========", result.getAllErrors());
            model.addAttribute("departments", departmentService.getParentDepartment());

            return "admin/authority/edit";
        }

        if(!ObjectUtils.isEmpty(account.getDepartment())) {
            Department department = account.getDepartment();
            if(!ObjectUtils.isEmpty(department.getParentDepartment())) {
                account.setDeptName(department.getParentDepartment().getName());
                account.setTeamName(department.getName());
            }else {
                account.setDeptName(department.getName());
                account.setTeamName(null);
            }
        }
        userService.saveOrUpdate(account);
        attributes.addFlashAttribute("message", "[" + account.getUsername() + "]의 정보가 수정되었습니다.");
        sessionStatus.setComplete();

        return "redirect:/admin/authority/users";
    }

//    @PostMapping("/authority/users/sync")
//    @Transactional
//    public String sync(RedirectAttributes attributes) {
//        userService.sync();
//        attributes.addFlashAttribute("message", "조직도 정보가 업데이트 되었습니다.");
//
//        return "redirect:/admin/authority/users";
//    }


    //교육 진행 여부 설정 변경.
    @PutMapping("/authority/users/{id}/training")
    @ResponseBody
    public Map<String, String> setTraining(@PathVariable("id") Integer id) {
        Map<String, String> result = new HashMap<>();
        Optional<Account> optionalUser = userRepository.findById(id);
        if(optionalUser.isPresent()) {
            Account user = optionalUser.get();
            user.setTraining(!user.isTraining());

            userRepository.save(user);
            result.put("result", "success");
            result.put("id", id.toString());
            result.put("name", user.getName());
            result.put("className", user.isTraining() ? "success" : "default");
        } else {
            result.put("result", "fail");
        }

        return result;
    }

    @PutMapping("/authority/users/{id}/receiveEmail")
    @ResponseBody
    public Map<String, String> setReceiveEmail(@PathVariable("id") Integer id) {
        Map<String, String> result = new HashMap<>();
        Optional<Account> optionalUser = userRepository.findById(id);
        if(optionalUser.isPresent()) {
            Account user = optionalUser.get();
            user.setReceiveEmail(!user.isReceiveEmail());

            userRepository.save(user);
            result.put("result", "success");
            result.put("id", id.toString());
            result.put("name", user.getName());
            result.put("className", user.isReceiveEmail() ? "success" : "default");
        } else {
            result.put("result", "fail");
        }

        return result;
    }
}
