package io.disposechat.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@ActiveProfiles("integration")
@TestConfiguration
@ConfigurationProperties(prefix = "auth0.test")
@Setter
@Getter
public class Auth0TestClient {

    @Value("${auth0.audience}")
    String audience;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String domainUri;

    private String clientId;
    private String clientSecret;
    private String grantType;
    private String scope;
    private String username;
    private String password;
    private String expiredToken;

    Mono<String> requestValidAccessToken(){
        return WebClient.builder()
                .baseUrl(domainUri)
                .build()
                .post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("grant_type", grantType)
                        .with("username", username)
                        .with("password", password)
                        .with("audience", audience)
                        .with("scope", scope)
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                ).retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .map(map -> map.get("access_token"));
    }

    Mono<String> requestExpiredAccessToken(){
        return Mono.just(expiredToken);
    }
}
