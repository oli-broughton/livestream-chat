package io.disposechat.messaging;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest
class MessageControllerITest {

    private static RSocketRequester requester;

    @BeforeAll
    public static void setupUp(@Autowired RSocketRequester.Builder builder, @Value("${spring.rsocket.server.port}") Integer port) {
        //https://github.com/rsocket/rsocket-java/issues/1018
        Hooks.onErrorDropped((throwable) -> {
        });
        requester = builder.tcp("localhost", port);
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

        Flux<Message> send = Flux.interval(Duration.ofMillis(100)).map(aLong -> message);

        Flux<Message> result = requester
                .route(receiveRoute)
                .data(send)
                .retrieveFlux(Message.class);


        StepVerifier.create(result).thenConsumeWhile(message1 -> true, System.out::println).expectNextCount(10).thenCancel().verify();
    }
}