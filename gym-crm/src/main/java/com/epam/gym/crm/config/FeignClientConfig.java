package com.epam.gym.crm.config;

import com.epam.gym.crm.context.TransactionContext;
import com.epam.gym.crm.service.security.JwtProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfig implements RequestInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public void apply(RequestTemplate template) {
        String txId = TransactionContext.get();
        if (txId != null) {
            template.header("X-Transaction-Id", txId);
        }

        String systemToken = jwtProvider.generateInternalServiceToken();

        template.header("Authorization", "Bearer " + systemToken);
    }
}