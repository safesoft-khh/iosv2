package com.cauh.iso;

import com.cauh.common.component.CustomAuditorAware;
import com.cauh.common.entity.Account;
import com.cauh.iso.component.CurrentUserComponent;
import com.cauh.iso.property.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.support.ErrorPageFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

//@Configuration
@SpringBootApplication
@ComponentScan({"com.cauh"})
@EnableJpaRepositories(value = {"com.cauh.common.repository", "com.cauh.iso.repository"},
        repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
@EntityScan({"com.cauh.common.domain", "com.cauh.iso.domain", "com.cauh.common.entity"})
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditorAware", modifyOnCreate = false)
@EnableConfigurationProperties({
        FileStorageProperties.class
})
@EnableScheduling
public class ISOApplication {

    @Autowired
    private CustomAuditorAware auditorAware;

    @Autowired
    private CurrentUserComponent currentUserComponent;

    public static void main(String[] args) {
        SpringApplication.run(ISOApplication.class, args);
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return auditorAware;
    }

    @Bean
    public ErrorPageFilter errorPageFilter() {
        return new ErrorPageFilter();
    }

    @Bean
    public FilterRegistrationBean disableSpringBootErrorFile(ErrorPageFilter filter) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }



    /**
     * BATCH - SOP
     * DEVELOPMENT(???????????? - ??????) -> ????????? ?????????
     * APPROVED -> EFFECTIVE
     * REVISION -> effective / ?????? effective sop -> superseded ??????
     *
     *
     * RETIREMENT -> SUPERSEDED
     */

    /**
     * BATCH - RD
     * DEVELOPMENT(???????????? - ??????) -> ????????? ?????????
     * APPROVED -> EFFECTIVE
     * REVISION(???????????? - ??????) -> 1. ????????? ????????? -> approved ????????? ??????(effective date) -> ?????? rd superseded ?????? ??????(?????? ????????? ?????? ?????? supersed ?????? ?????? ??????)
     */
}
