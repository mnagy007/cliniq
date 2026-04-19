package com.cliniq.bootstrap.config;

import com.cliniq.shared.domain.TenantId;
import com.cliniq.web.security.TenantContext;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantFilterAspect {

    private final EntityManager entityManager;

    public TenantFilterAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Before("execution(* com.cliniq.persistence..adapter.*Adapter.*(..))")
    public void enableTenantFilter() {
        TenantId tenantId = TenantContext.get();
        if (tenantId != null) {
            entityManager.unwrap(Session.class)
                    .enableFilter("tenantFilter")
                    .setParameter("tenantId", tenantId.value());
        }
    }
}