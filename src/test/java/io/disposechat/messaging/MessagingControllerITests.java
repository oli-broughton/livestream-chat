package io.disposechat.messaging;

import io.rsocket.exceptions.ApplicationErrorException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.BearerTokenMetadata;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Map;

@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
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

    @AfterAll
    public static void tearDown() {
        requester.dispose();
    }

    @Test
    void sendMessage(){
            var sendRoute = "send";
            var testMessage = "test message";

           var request =  requestAccessToken()
                    .flatMap(token -> requester.
                            route(sendRoute)
                            .metadata(token, BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE)
                            .data(testMessage)
                            .retrieveMono(Void.class));


            StepVerifier.create(request).verifyComplete();
    }

    @Test
    void sendMessageExpiredToken() {
        var sendRoute = "send";
        var testMessage = "test message";


        // todo - move to properties file
        var invalidToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ill5MHEzWkg2cHV" +
                "JUnRTNE1OOHFpNiJ9.eyJodHRwczovL2Rpc3RyaWJ1dGVkY2hhdC5pby9hcGkvdXNlcm5hbWU" +
                "iOiJ0ZXN0dXNlcjEiLCJpc3MiOiJodHRwczovL2Rldi1wdHlldGU2Ny51cy5hdXRoMC5jb20vIiwic3ViIjoiYXV0aDB8NjExM2U1NGU" +
                "yYjFjNWQwMDY5NDQ5ZTBkIiwiYXVkIjoiaHR0cHM6Ly9kaXN0cmlidXRlZGNoYXQuaW" +
                "8vYXBpIiwiaWF0IjoxNjI4NzY3MzQ2LCJleHAiOjE2Mjg3Njc0MDYsImF6cCI6ImlqRWd6" +
                "QUVTSUpIRWNPYUNhS2tnTk5EVjBMRXFDQk5VIiwic2NvcGUiOiJyZWFkOmN1cnJlbnRfdXNlciIsImd0eSI6InBhc3N3b3JkIn0" +
                ".anq0HVVIkSOs0swGLHjC0kctSloXuresBxCOIrH761fN6QyqVoul" +
                "0b23DWnD-uYyLWhqCJWv0mDs543JsMZ79uXSnw0Vv4IOvGIsCNlbey0dBbfxX64pqbbZsmvCfzPhSR_w2RYOnuXNrReIefTlPvx8iCUqJ2GhRYtOX" +
                "-q28D68afuXbB4wmTX9UaVxacohZ00ZBmMLZnsf_BpUqbbA5IWZ1kkuH_f2I1L1EOS5JDk--4xz4Qr4Jh1BaIYVuwfOesPPgKpjoan2C8irA8vQBkVVYb2cY-" +
                "ctfuJsuYNbbVcAl5SNf-HU01jCy7T2aCc7r_ypQuIsTsyqnP5REHaLOg";

        // todo - maybe create user test class
        var response = requester.
                route(sendRoute)
                .metadata(invalidToken, BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE)
                .data(testMessage)
                .retrieveMono(Void.class);

        StepVerifier.create(response).expectError(ApplicationErrorException.class).verify();
    }

    @Test
    void receiveMessage() {
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
}