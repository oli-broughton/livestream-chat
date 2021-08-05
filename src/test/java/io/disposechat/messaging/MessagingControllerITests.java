package io.disposechat.messaging;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.net.URI;

@SpringBootTest
class MessagingControllerITests {

    private static RSocketRequester requester;

    @BeforeAll
    public static void setupUp(@Autowired RSocketRequester.Builder builder) {

        //https://github.com/rsocket/rsocket-java/issues/1018
        Hooks.onErrorDropped((throwable) -> {
        });

        requester = builder.websocket(URI.create("ws://localhost:8080/rs"));
    }

    @AfterAll
    public static void tearDown() {
        requester.dispose();
    }

    @Test
    void sendMessage() {
        var sendRoute = "send";
        var receiveRoute = "receive";
        var message = new Message("test", "test", "test");

        var messages = requester.route(receiveRoute)
                .retrieveFlux(Message.class);

        requester.route(sendRoute).data(message).send().subscribeOn(Schedulers.single()).subscribe();
        StepVerifier.create(messages).consumeNextWith(System.out::println).thenCancel().verify();
    }
}