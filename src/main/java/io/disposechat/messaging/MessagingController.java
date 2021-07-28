package io.disposechat.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
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
    void onConnect(RSocketRequester requester) {
        Objects.requireNonNull(requester.rsocket())
                .onClose()
                .doOnError(error -> log.warn(requester.rsocketClient() + " Closed"))
                .doFinally(consumer -> log.info(requester.rsocketClient() + " Disconnected"))
                .subscribe();

        log.info(requester.rsocketClient() + " Connected");
    }

    @MessageMapping("send")
    void send(RSocketRequester requester, @Payload Message message) {
        messagingService.send(message);
        log.info(requester.rsocketClient() + " sent " + message);
    }

    @MessageMapping("receive")
    Flux<Message> receive() {
        return messagingService.receive();
    }
}