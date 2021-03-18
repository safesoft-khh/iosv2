package com.cauh.iso.controller;

import com.cauh.common.entity.Account;
import com.cauh.common.repository.UserRepository;
import com.cauh.common.security.annotation.CurrentUser;
import com.cauh.common.service.UserService;
import com.cauh.iso.domain.UserPasswordDTO;
import com.cauh.iso.validator.UserForgotPasswordValidator;
import com.cauh.iso.validator.UserPasswordChangeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
@SessionAttributes({"user"})
@RequiredArgsConstructor
public class LoginController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserPasswordChangeValidator userPasswordChangeValidator;
    private final UserForgotPasswordValidator userForgotPasswordValidator;
    private final PasswordEncoder passwordEncoder;

    @Value("${site.image-logo}")
    private String imageLogo;

    @Value("${site.login-image}")
    private String loginImage;

    @Value("${site.link}")
    private String siteLink;

    @Value("${meta.keywords}")
    private String keywords;

    @Value("${meta.description}")
    private String description;

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        if(ObjectUtils.isEmpty(authentication)) {
            model.addAttribute("imageLogo", imageLogo);
            model.addAttribute("siteLink", siteLink);
            model.addAttribute("loginImage", loginImage);
            model.addAttribute("description", description);
            model.addAttribute("keywords", keywords);

            return "login";
        } else {
            return "redirect:/notice";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        //2021-03-16 YSH :: 설정된 Image Logo 사용
        model.addAttribute("imageLogo", imageLogo);
        model.addAttribute("user", new Account());

        return "common/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@ModelAttribute("user") Account user, SessionStatus status,
                                 RedirectAttributes attributes, BindingResult result){
        userForgotPasswordValidator.validate(user, result);
        if(result.hasErrors()) {
            log.debug("User Errors : {}", result.getAllErrors());
            return "common/forgot-password";
        }

        Account resetUser = userRepository.findByUsernameAndEmail(user.getUsername(), user.getEmail()).get();
        userService.userPasswordReset(resetUser);

        return "redirect:/login?passwordReset";
    }

    @GetMapping("/password-change")
    public String passwordChange(@CurrentUser Account user, Model model){

        if(user.getCredentialsExpiredDate().after(new Date())) {
            log.debug("현재 비밀번호 기한 : {}, 만료가 아닌 경우 홈화면으로 이동.", user.getCredentialsExpiredDate());
            return "redirect:/";
        }

        model.addAttribute("user", Account.builder().id(user.getId()).name(user.getName()).build());
        return "common/pwchange";
    }

    @GetMapping("/password-change/skip")
    public String passwordChangeSkip(@CurrentUser Account user) {
        user.setPasswordExpiredIgnore(true);

        return "redirect:/notice";
    }

    @PostMapping("/password-change")
    public String passwordChangeProc(@CurrentUser Account currentUser,
                                     @ModelAttribute("user") Account user,
                                     SessionStatus status,
                                     RedirectAttributes attributes, BindingResult result) {

//        log.debug("New Password : {}", user.getNewPassword());
//        log.debug("Current Password : {}", user.getPassword());
        UserPasswordDTO userPasswordDTO = new UserPasswordDTO(currentUser, user);
        userPasswordChangeValidator.validate(userPasswordDTO, result);

        if(result.hasErrors()){
            log.debug("Errors : {}", result.getAllErrors());
            return "common/pwchange";
        }

        //비밀번호 기한 설정 및 비밀번호 변경
        currentUser.setCredentialsExpiredDate(Date.from(LocalDate.now().plusDays(90).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        currentUser.setPassword(passwordEncoder.encode(user.getNewPassword()));
        Account savedUser = userRepository.save(currentUser);

        log.info("저장된 유저 : {}", savedUser);
        log.info("저장된 비밀번호 : {}", savedUser.getPassword());
        log.info("저장된 비밀번호 유효기간 : {}", savedUser.getCredentialsExpiredDate());

        status.setComplete();
        return "redirect:/";
    }

    /**
     * Password change 시, 현재 Password와 비교하기.
     * @param data
     * @return
     */
    @PostMapping("/ajax/password-chanage/validation")
    @ResponseBody
    public Map<String, Boolean> passwordValidation(@CurrentUser Account user, @RequestParam("data") String data){
        Map<String, Boolean> map = new HashMap<>();
        boolean result = true;
        if(passwordEncoder.matches(data, user.getPassword())){
            result = false;
        }

        map.put("valid", result);
        return map;
    }
}