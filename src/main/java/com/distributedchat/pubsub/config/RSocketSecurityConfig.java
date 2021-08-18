package com.distributedchat.pubsub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
public class RSocketSecurityConfig {

    @Value("${auth0.audience}")
    String audience;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuer;
    @Value("${auth0.username-claim}")
    String usernameClaim;

    @Bean
    public PayloadSocketAcceptorInterceptor rsocketInterceptor(RSocketSecurity rSocketSecurity) {
        return rSocketSecurity.authorizePayload(authorize ->
                        authorize.route("publish").authenticated()
                                .anyExchange().permitAll()
                )
                .jwt(Customizer.withDefaults())
                .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {

        var reactiveJwtDecoder = (NimbusReactiveJwtDecoder) ReactiveJwtDecoders.fromOidcIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = (jwt) -> {
            OAuth2Error error = new OAuth2Error("invalid_token", "The required audience is missing", null);
            if (jwt.getAudience().contains(audience)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(error);
        };

        OAuth2TokenValidator<Jwt> usernameValidator = (jwt) -> {
            OAuth2Error error = new OAuth2Error("invalid_token", "The required username is missing", null);
            if (jwt.getClaimAsString(usernameClaim) != null) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(error);
        };

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> compositeValidator = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator, usernameValidator);

        reactiveJwtDecoder.setJwtValidator(compositeValidator);

        return reactiveJwtDecoder;
    }

    @Bean
    RSocketMessageHandler messageHandler(RSocketStrategies strategies) {
        RSocketMessageHandler mh = new RSocketMessageHandler();
        mh.getArgumentResolverConfigurer().addCustomResolver(
                new AuthenticationPrincipalArgumentResolver());
        mh.setRouteMatcher(new PathPatternRouteMatcher());
        mh.setRSocketStrategies(strategies);
        return mh;
    }
}
