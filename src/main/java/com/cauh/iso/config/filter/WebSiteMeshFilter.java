package com.cauh.iso.config.filter;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;

public class WebSiteMeshFilter extends ConfigurableSiteMeshFilter {
    @Override
    protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
        builder
//                .addDecoratorPath("/login", "/WEB-INF/decorators/loginDecorator.jsp")
                .addDecoratorPath("/admin/**", "/WEB-INF/decorators/adminDecorator.jsp")
                .addDecoratorPath("/please-enter-your-access-code", "/WEB-INF/decorators/loginDecorator.jsp")
                .addDecoratorPath("/internal-user-terms-of-use", "/WEB-INF/decorators/loginDecorator.jsp")
                .addDecoratorPath("/agreement-to-collect-and-use-personal-information", "/WEB-INF/decorators/loginDecorator.jsp")
                .addDecoratorPath("/non-disclosure-agreement-for-sop", "/WEB-INF/decorators/loginDecorator.jsp")
                .addDecoratorPath("/confidentiality-pledge", "/WEB-INF/decorators/loginDecorator.jsp")
                .addDecoratorPath("/agreements-withdrawal", "/WEB-INF/decorators/loginDecorator.jsp")
                .addDecoratorPath("/agreements-withdrawal/**", "/WEB-INF/decorators/loginDecorator.jsp")
                .addDecoratorPath("/password-change", "/WEB-INF/decorators/loginDecorator.jsp")
                .addDecoratorPath("/forgot-password", "/WEB-INF/decorators/loginDecorator.jsp")
                .addDecoratorPath("/signUp", "/WEB-INF/decorators/loginDecorator.jsp")
                .addDecoratorPath("/**", "/WEB-INF/decorators/defaultDecorator.jsp")
//                .addDecoratorPath("/external/**", "/WEB-INF/decorators/adminDecorator.jsp")
                .addExcludedPath("/javadoc/*")
//                .addExcludedPath("/password-change")
//                .addExcludedPath("/signUp")
                .addExcludedPath("/brochures/*")
                .addExcludedPath("/api/*")
                .addExcludedPath("/login")
                .addExcludedPath("/viewer/*")
                .addExcludedPath("/ajax/*")
                .addExcludedPath("/static/**")
                .addExcludedPath("/favicon.ico");
    }
}