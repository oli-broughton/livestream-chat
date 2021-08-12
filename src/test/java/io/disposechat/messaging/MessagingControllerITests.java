package io.disposechat.messaging;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.BearerTokenMetadata;
import org.springframework.util.MimeType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.awt.print.Book;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestRedisConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MessagingControllerITests {

    @Value("${auth0.audience}")
    String audience;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String domainUri;
    @Value("${auth0.test.client-id}")
    String clientId;
    @Value("${auth0.test.client-secret}")
    String clientSecret;
    @Value("${auth0.test.grant-type}")
    String grant_type;
    @Value("${auth0.test.scope}")
    String scope;
    @Value("${auth0.test.username}")
    String username;
    @Value("${auth0.test.password}")
    String password;

    private static RSocketRequester requester;


    @BeforeAll
    public void setupUp(@Autowired RSocketRequester.Builder builder) {

        //Hide cancellation exception from test log https://github.com/rsocket/rsocket-java/issues/1018
        Hooks.onErrorDropped((throwable) -> {
        });
        requester = builder.websocket(URI.create("ws://localhost:8080/rs"));
    }

    public Mono<String> requestAccessToken() {
        return WebClient.builder()
                .baseUrl(domainUri)
                .build()
                .post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("grant_type", grant_type)
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

    @AfterAll
    public static void tearDown() {
        requester.dispose();
    }

    @Test
    void validToken() {

    }

    @Test
    void sendMessage() {
        var sendRoute = "send";
        var testMessage = "test message";

        // todo - maybe create user test class
        var response  = requestAccessToken()
                .flatMap(token -> requester.
                        route(sendRoute)
                        .metadata(token, BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE)
                        .data(testMessage)
                        .retrieveMono(Boolean.class));

        StepVerifier.create(response).expectNext(true).verifyComplete();

    }

    @Test
    void receiveMessage(){
        var sendRoute = "send";
        var retrieveRoute = "receive";
        var testMessage = "test message";

        var receivedMessages = requester
                .route(retrieveRoute)
                .retrieveFlux(Message.class);

        requestAccessToken()
                .flatMap(token -> requester.
                        route(sendRoute)
                        .metadata(token, BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE)
                        .data(testMessage)
                        .retrieveMono(Boolean.class))
                .subscribeOn(Schedulers.single())
                .subscribe();

        StepVerifier.create(receivedMessages).expectNext(new Message(username, testMessage)).thenCancel().verify();
    }
}