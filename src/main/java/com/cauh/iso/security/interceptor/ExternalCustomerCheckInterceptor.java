package com.cauh.iso.security.interceptor;

import com.cauh.common.entity.Account;
import com.cauh.common.entity.constant.UserType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class ExternalCustomerCheckInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //로그인 되었을 때만 진행
        if (authentication.getPrincipal() instanceof Account) {
            Account user = (Account) authentication.getPrincipal();

            if (user.getUserType() == UserType.AUDITOR && user.isActivate() == true) {
                String uri = request.getRequestURI();
                log.debug("@PreHandle @URI : {}", uri);
                if (user.isAgreementCollectUse()) {
                    log.trace("@PreHandle @Username : {} Agreement to Collect 1Use 동의 처리 되어 있음.", user.getUsername());
                    if (user.isNonDisclosureAgreement()) {
                        if (!uri.startsWith("/external") && !uri.startsWith("/rf/view") && !uri.startsWith("/sop/effective/viewer") && !uri.startsWith("/sop/superseded/viewer")) {
                            log.info("@PreCheck SOP, Digital Binder 를 제외한 메뉴는 접근 불가 : [{}]", uri);

                            response.sendRedirect("/external/sop/effective");
                            return false;
                        }
                    }
                }
            }
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //로그인 되었을 때만 진행
        if (authentication.getPrincipal() instanceof Account) {
            Account user = (Account) authentication.getPrincipal();
            if (user.getUserType() == UserType.AUDITOR && user.isActivate() == false) {
                modelAndView.setViewName("redirect:/please-enter-your-access-code");
            } else if (user.getUserType() == UserType.AUDITOR && user.isActivate() == true) {
                String uri = request.getRequestURI();
                log.debug("@URI : {}", uri);
                if (user.isAgreementCollectUse()) {
                    log.trace("@Username : {} Agreement to Collect Use 동의 처리 되어 있음.", user.getUsername());
//
                    if (!user.isNonDisclosureAgreement()) {
//                    if (uri.equals("/") || (!uri.startsWith("/sop")) && !uri.startsWith("/digital-binder")) {
//                        modelAndView.setViewName("redirect:/sop/effective");
//                    }
//                } else {
                        log.info("@Username : {} Non-Disclosure Agreement for SOP 동의 기록 없음. 동의 페이지로 이동", user.getUsername());
                        modelAndView.setViewName("redirect:/non-disclosure-agreement-for-sop");
                    }
                } else {
                    log.info("@Username : {} Agreement to Collect Use 동의 기록 없음. 동의 페이지로 이동", user.getUsername());
                    modelAndView.setViewName("redirect:/agreement-to-collect-and-use-personal-information");
                }
            }
        }

        super.postHandle(request, response, handler, modelAndView);
    }
}
