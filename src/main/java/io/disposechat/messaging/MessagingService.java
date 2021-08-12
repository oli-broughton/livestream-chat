package io.disposechat.messaging;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessagingService {
    Mono<Boolean> send(Message message);
    Flux<Message> receive();
}
