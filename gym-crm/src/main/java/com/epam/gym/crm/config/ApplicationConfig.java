package com.epam.gym.crm.config;

import com.epam.gym.crm.filter.HttpMetricsFilter;
import com.epam.gym.crm.filter.RequestResponseLoggingFilter;
import com.epam.gym.crm.filter.TransactionFilter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class ApplicationConfig {

    @Bean
    public FilterRegistrationBean<TransactionFilter> transactionFilter() {
        FilterRegistrationBean<TransactionFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TransactionFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestResponseLoggingFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<HttpMetricsFilter> httpMetricsFilter(MeterRegistry meterRegistry) {
        FilterRegistrationBean<HttpMetricsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HttpMetricsFilter(meterRegistry));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return registration;
    }
}