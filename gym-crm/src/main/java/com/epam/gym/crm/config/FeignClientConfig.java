package com.epam.gym.crm.config;

import com.epam.gym.crm.context.TransactionContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String txId = TransactionContext.get();
        if (txId != null) {
            template.header("X-Transaction-Id", txId);
        }
    }
}