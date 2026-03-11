package com.iprody.paymentserviceapp;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public class TestJwtFactory {

    public static RequestPostProcessor jwtWithRole(String username, String... roles) {
        return jwt()
                .jwt(jwt -> {
                    jwt.claim("sub", "123");
                    jwt.claim("preferred_username", username);
                    jwt.claim("realm_access", Map.of("roles", List.of(roles)));
                })
                .authorities(
                        Stream.of(roles)
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                                .collect(Collectors.toList())
                );
    }
}
