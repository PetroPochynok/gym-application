package com.epam.gym.crm.aspect;

import com.epam.gym.crm.context.TransactionContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OperationLoggingAspect {

    private static final Logger LOG = LoggerFactory.getLogger(OperationLoggingAspect.class);

    @Before("execution(* com.epam.gym.crm.service..*.*(..))")
    public void logBeforeOperation(JoinPoint joinPoint) {
        String txId = TransactionContext.get();
        String methodName = joinPoint.getSignature().toShortString();

        LOG.info("[txId={}] Operation level - Executing service method: {}", txId, methodName);
    }
}