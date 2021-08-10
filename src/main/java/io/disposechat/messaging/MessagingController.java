package io.disposechat.messaging;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.Objects;

@Controller
@Slf4j
public class MessagingController {

    private final MessagingService messagingService;

    public MessagingController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @ConnectMapping
    void onConnect(RSocketRequester requester, @AuthenticationPrincipal Jwt token) {
        Objects.requireNonNull(requester.rsocket(), "rsocket  should not be null")
                .onClose()
                .doOnError(error -> log.warn(requester.rsocketClient() + " Closed"))
                .doFinally(consumer -> log.info(requester.rsocketClient() + " Disconnected"))
                .subscribe();

    }

    @SneakyThrows
    @MessageMapping("send")
    void send(RSocketRequester requester, Message message, @AuthenticationPrincipal Jwt token) {
        log.info(token.getSubject());
        messagingService.send(message);
        log.info(requester.rsocketClient() + " sent " + message);
    }

    @MessageMapping("receive")
    Flux<Message> receive() {
        return messagingService.receive();
    }
}