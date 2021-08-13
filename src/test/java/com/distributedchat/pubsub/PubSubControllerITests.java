package com.distributedchat.pubsub;

import io.rsocket.exceptions.ApplicationErrorException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.BearerTokenMetadata;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;

@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableConfigurationProperties(value = Auth0TestClient.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class PubSubControllerITests {
    @Autowired
    Auth0TestClient auth0TestClient;

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
    void requestValidToken(){
        var accessTokenRequest = auth0TestClient.requestValidAccessToken();
        StepVerifier.create(accessTokenRequest).
                consumeNextWith(accessToken->Assertions.assertFalse(accessToken.isEmpty()) )
                .verifyComplete();
    }

    @Test
    void sendMessage() {
        var publishRoute = "publish";
        var testMessage = "test message";

        var request = auth0TestClient.requestValidAccessToken()
                .flatMap(token -> requester.
                        route(publishRoute)
                        .metadata(token, BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE)
                        .data(testMessage)
                        .retrieveMono(Void.class));

        StepVerifier.create(request).verifyComplete();
    }

    @Test
    void sendMessageExpiredToken() {
        var publishRoute = "publish";
        var testMessage = "test message";

        var response = auth0TestClient.requestExpiredAccessToken()
                .flatMap(token -> requester.
                        route(publishRoute)
                        .metadata(token, BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE)
                        .data(testMessage)
                        .retrieveMono(Void.class));

        StepVerifier.create(response)
                .expectError(ApplicationErrorException.class)
                .verify(Duration.ofSeconds(1));
    }

    @Test
    void receiveMessage() {
        var publishRoute = "publish";
        var subscribeRoute = "subscribe";
        var testMessage = "test message";

        var receivedMessages = requester
                .route(subscribeRoute)
                .retrieveFlux(Message.class)
                .cache();

        auth0TestClient.requestValidAccessToken()
                .flatMap(token -> requester.
                        route(publishRoute)
                        .metadata(token, BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE)
                        .data(testMessage)
                        .retrieveMono(Void.class))
                        .subscribe();

        StepVerifier.create(receivedMessages)
                .expectNext(new Message(auth0TestClient.getUsername(), testMessage))
                .thenCancel()
                .verify(Duration.ofSeconds(1));

    }
}