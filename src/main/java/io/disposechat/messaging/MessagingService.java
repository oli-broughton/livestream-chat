package io.disposechat.messaging;

import reactor.core.publisher.Flux;

public interface MessagingService {
    void send(Message message);
    Flux<Message> receive();
}
