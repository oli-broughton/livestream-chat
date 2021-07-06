package io.disposechat.messaging;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

@Controller
@Slf4j
public class MessageController {

    private final Sender sender;
    private final Receiver receiver;
    private final String queue;
    private final String exchange;

    public MessageController(Sender sender, Receiver receiver, String queue, String exchange) {
        this.sender = sender;
        this.receiver = receiver;
        this.queue = queue;
        this.exchange = exchange;
    }

//    @MessageMapping("send")
//    void send(Flux<Message> messages) {
//        messages.map(message -> new OutboundMessage(exchange, "", SerializationUtils.serialize(message)))
//                .flatMap(outboundMessage -> this.sender.send(Mono.just(outboundMessage)))
//                .subscribe();
//    }

    @ConnectMapping
    void onConnect(RSocketRequester requester)
    {
        log.info("connected" +  requester.rsocketClient());
    }

    @MessageMapping("send")
    void send( RSocketRequester requester, Message message) {
        log.info(requester.rsocketClient().toString());
        this.sender.send(Mono.just(new OutboundMessage(exchange, "", SerializationUtils.serialize(message))))
                .subscribe();
    }

    @MessageMapping("receive")
    Flux<Message> receive() {



        // todo - look into json serialization / wrappers
        return this.receiver.consumeAutoAck(queue)
                .map(delivery -> SerializationUtils.deserialize(delivery.getBody()));
    }
}
