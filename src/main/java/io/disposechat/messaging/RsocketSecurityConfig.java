package io.disposechat.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
public class RsocketSecurityConfig {

  @Value("${jwt.key}")
    String key;

    @Bean
    public PayloadSocketAcceptorInterceptor rsocketInterceptor(RSocketSecurity rSocketSecurity) {
        return rSocketSecurity.authorizePayload(authorize ->
                        authorize.anyExchange().authenticated())
                .jwt( jwtSpec -> {
                    try {
                        jwtSpec.authenticationManager(jwtReactiveAuthenticationManager(reactiveJwtDecoder()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), mac.getAlgorithm());

        return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    public JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager(ReactiveJwtDecoder reactiveJwtDecoder) {
        JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager = new JwtReactiveAuthenticationManager(reactiveJwtDecoder);

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLES_");
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        jwtReactiveAuthenticationManager.setJwtAuthenticationConverter( new ReactiveJwtAuthenticationConverterAdapter(authenticationConverter));
        return jwtReactiveAuthenticationManager;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges ->
                        exchanges
                                .anyExchange().permitAll()
                ).csrf().disable();

        return http.build();
    }

    @Bean
    RSocketMessageHandler messageHandler(RSocketStrategies strategies) {
        return getMessageHandler(strategies);
    }

    protected RSocketMessageHandler getMessageHandler(RSocketStrategies strategies) {
        RSocketMessageHandler mh = new RSocketMessageHandler();
        mh.getArgumentResolverConfigurer().addCustomResolver(
                new AuthenticationPrincipalArgumentResolver());
        mh.setRouteMatcher(new PathPatternRouteMatcher());
        mh.setRSocketStrategies(strategies);

        return mh;
    }
}
