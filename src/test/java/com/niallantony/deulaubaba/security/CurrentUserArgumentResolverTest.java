package com.niallantony.deulaubaba.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserArgumentResolverTest {

    private CurrentUserArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CurrentUserArgumentResolver();
        SecurityContextHolder.clearContext();
    }

    static class DummyController {
        public void method(@CurrentUser String userId, String other) {}
    }

    private MethodParameter getAnnotatedParameter() throws NoSuchMethodException {
        Method method = DummyController.class.getMethod("method", String.class, String.class);
        return new MethodParameter(method, 0);
    }

    private MethodParameter getUnannotatedParameter() throws NoSuchMethodException {
        Method method = DummyController.class.getMethod("method", String.class, String.class);
        return new MethodParameter(method, 1);
    }

    @Test
    void supportsParameter_withAnnotationAndString_returnsTrue() throws Exception {
        assertTrue(resolver.supportsParameter(getAnnotatedParameter()));
    }

    @Test
    void supportsParameter_withoutAnnotation_returnsFalse() throws Exception {
        assertFalse(resolver.supportsParameter(getUnannotatedParameter()));
    }

    @Test
    void resolveArgument_withJwtInContext_returnsSubClaim() throws Exception {
        Jwt jwt = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                Map.of("sub", "user-123")
        );
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(jwt, null)
        );

        Object result = resolver.resolveArgument(getAnnotatedParameter(), null, null, null);
        assertEquals("user-123", result);
    }

    @Test
    void resolveArgument_withoutJwt_returnsNull() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);

        Object result = resolver.resolveArgument(getAnnotatedParameter(), null, null, null);
        assertNull(result);
    }
}