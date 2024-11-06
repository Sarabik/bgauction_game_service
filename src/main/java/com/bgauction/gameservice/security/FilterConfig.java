package com.bgauction.gameservice.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    private final ServiceKeyFilter serviceKeyFilter;

    public FilterConfig(ServiceKeyFilter serviceKeyFilter) {
        this.serviceKeyFilter = serviceKeyFilter;
    }

    @Bean
    public FilterRegistrationBean<ServiceKeyFilter> serviceKeyFilterRegistration() {
        FilterRegistrationBean<ServiceKeyFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(serviceKeyFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
