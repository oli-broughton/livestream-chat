package io.disposechat.messaging;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    final MessagePublisher messagePublisher;

    public MessageController(MessagePublisher messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    @MessageMapping("/message")
    void message(Message m)
    {
        messagePublisher.publishMessage(m);
    }

}
