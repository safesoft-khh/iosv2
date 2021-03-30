package com.cauh.iso.config;


import com.cauh.iso.config.filter.WebSiteMeshFilter;
import com.cauh.iso.security.interceptor.ExternalCustomerCheckInterceptor;
import com.cauh.iso.security.interceptor.LoginPostCheckInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.theme.CookieThemeResolver;
import org.springframework.web.servlet.theme.ThemeChangeInterceptor;

import java.util.Arrays;

@Configuration
@EnableWebMvc
@Slf4j
@MapperScan(value={"com.**.mapper"})
public class WebMvcConfig implements WebMvcConfigurer {
    //Spring Theme 기능 적용
    @Bean
    public ResourceBundleThemeSource themeSource(){
        ResourceBundleThemeSource themeSource = new ResourceBundleThemeSource();
        themeSource.setDefaultEncoding("UTF-8");
        themeSource.setBasenamePrefix("themes.");
        return themeSource;
    }

    @Bean
    public CookieThemeResolver themeResolver(){
        CookieThemeResolver resolver = new CookieThemeResolver();
        resolver.setDefaultThemeName("c-navy");
        resolver.setCookieName("theme-cookie");
        return resolver;
    }

    @Bean
    public ThemeChangeInterceptor themeChangeInterceptor(){
        ThemeChangeInterceptor interceptor = new ThemeChangeInterceptor();
        interceptor.setParamName("theme");
        return interceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/static/**", "/favicon.ico")
                .addResourceLocations("classpath:/resources/");
    }

    @Bean
    public FilterRegistrationBean siteMeshFilter() {
        log.info("@siteMeshFilter()");
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        WebSiteMeshFilter siteMeshFilter = new WebSiteMeshFilter();
        filterRegistrationBean.setFilter(siteMeshFilter);

        return filterRegistrationBean;
    }

    @Bean
    public ExternalCustomerCheckInterceptor externalCustomerCheckInterceptor() {
        return new ExternalCustomerCheckInterceptor();
    }

    @Bean
    public LoginPostCheckInterceptor loginPostCheckInterceptor() {
        return new LoginPostCheckInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("language"); // Query string parameter name
        registry.addInterceptor(localeChangeInterceptor);

        registry.addInterceptor(themeChangeInterceptor());

        //외부사용자 전용 인터셉터
        registry.addInterceptor(externalCustomerCheckInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/login", "/signUp/**", "/logout", "/error", "/expired", "/invalidSession", "/api/**", "/favicon.ico", "/ajax/**",
                        "/denied",
                        "/please-enter-your-access-code",
                        "/agreement-to-collect-and-use-personal-information",
                        "/non-disclosure-agreement-for-sop");

        //YSH : 2020-1124 - Signature 미사용으로 인한 Interceptor 비활성화.
        //YSH : 2021-0106 - 다시 사용하기로 결정. - 로그인 이후 동작 제어.
        registry.addInterceptor(loginPostCheckInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/login", "/signUp/**", "/logout", "/error", "/expired", "/invalidSession", "/api/**", "/favicon.ico", "/ajax/**",
                        "/password-change",
                        "/forgot-password",
                        "/denied",
                        "/internal-user-terms-of-use",
                        "/user/signature");
    }

    @Bean
    public FilterRegistrationBean hiddenHttpMethodFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new HiddenHttpMethodFilter());
        filterRegistrationBean.setUrlPatterns(Arrays.asList("/*"));

        return filterRegistrationBean;
    }
}
