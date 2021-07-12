package io.disposechat.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
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
        Objects.requireNonNull(requester.rsocket()).onClose()
                .subscribe((close) -> log.info("disconnected" + requester.rsocketClient()));

        log.info("connected" + requester.rsocketClient());
    }

    @MessageMapping("send")
    void send(RSocketRequester requester,  Message message) {
        messagingService.send(message);
        log.info(requester.rsocketClient() + " sent " + message);
    }

    @MessageMapping("receive")
    Flux<Message> receive() {
        return messagingService.receive();
    }
}
