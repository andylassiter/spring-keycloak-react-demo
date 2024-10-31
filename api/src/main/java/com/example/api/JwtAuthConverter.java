package com.example.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Value("${jwt.auth.converter.principle-attribute}")
    private String principleAttribute;
    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {

        log.info("Principle Attribute: {}", principleAttribute);
        log.info("Resource ID: {}", resourceId);
        log.info("Converting JWT: {}", jwt);

        Collection<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractRoles(jwt).stream()
        ).collect(Collectors.toSet());

        AbstractAuthenticationToken token = new JwtAuthenticationToken(
                jwt,
                authorities,
                getPrincipleClaimName(jwt)
        );

        log.info("JWT converted: {}", token);
        log.info("JWT Roles: {}", authorities);

        return token;
    }

    private String getPrincipleClaimName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;
        if (principleAttribute != null) {
            claimName = principleAttribute;
        }
        return jwt.getClaim(claimName);
    }

    private Collection<? extends GrantedAuthority> extractRoles(Jwt jwt) {
        Collection<GrantedAuthority> roles = new HashSet<>();

        // Extract resource roles
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null && resourceAccess.get(resourceId) != null) {
            Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(resourceId);
            Collection<String> resourceRoles = (Collection<String>) resource.get("roles");
            if (resourceRoles != null) {
                roles.addAll(resourceRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toSet()));
            }
        }

        // Extract realm roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            Collection<String> realmRoles = (Collection<String>) realmAccess.get("roles");
            if (realmRoles != null) {
                roles.addAll(realmRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toSet()));
            }
        }

        return roles;
    }

//    private Collection<? extends GrantedAuthority> extractRoles(Jwt jwt) {
//        Map<String, Object> resourceAccess;
//        Map<String, Object> resource;
//        Collection<String> resourceRoles;
//        if (jwt.getClaim("resource_access") == null) {
//            return Set.of();
//        }
//        resourceAccess = jwt.getClaim("resource_access");
//
//        log.info("Resource Access: {}", resourceAccess);
//
//        if (resourceAccess.get(resourceId) == null) {
//            return Set.of();
//        }
//        resource = (Map<String, Object>) resourceAccess.get(resourceId);
//
//        resourceRoles = (Collection<String>) resource.get("roles");
//
//        log.info("Resource Roles: {}", resourceRoles);
//
//        return resourceRoles
//                .stream()
//                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//                .collect(Collectors.toSet());
//    }


}
