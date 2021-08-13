package com.distributedchat.pubsub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Controller
@Slf4j
public class PubSubController {

    @Value("${auth0.audience}")
    String audience;

    private final PubSubService messagingService;

    public PubSubController(PubSubService messagingService) {
        this.messagingService = messagingService;
    }

    @ConnectMapping
    void onConnect(RSocketRequester requester) {
        Objects.requireNonNull(requester.rsocket(), "rsocket  should not be null")
                .onClose()
                .doOnError(error -> log.warn(requester.rsocketClient() + " Closed"))
                .doFinally(consumer -> log.info(requester.rsocketClient() + " Disconnected"))
                .subscribe();
    }

    @MessageMapping("publish")
    Mono<Void> publish(String message, @AuthenticationPrincipal Mono<Jwt> token) {
        return token.map(jwt -> jwt.getClaimAsString(audience + "/username"))
                .flatMap(username -> messagingService.publish(new Message(username, message)));
    }

    @MessageMapping("subscribe")
    Flux<Message> subscribe() {
        return messagingService.subscribe();
    }
}