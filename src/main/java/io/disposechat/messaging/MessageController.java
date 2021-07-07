package io.disposechat.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.SerializationUtils;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Controller
@Slf4j
public class MessageController {

    private final ReactiveRedisTemplate<String, Message> reactiveTemplate;
    private final ChannelTopic messageTopic;
    private final ReactiveRedisMessageListenerContainer reactiveMsgListenerContainer;

    public MessageController(ReactiveRedisTemplate<String, Message> reactiveTemplate,
                             ReactiveRedisMessageListenerContainer reactiveMsgListenerContainer,
                             ChannelTopic messageTopic) {

        this.reactiveMsgListenerContainer = reactiveMsgListenerContainer;
        this.reactiveTemplate = reactiveTemplate;
        this.messageTopic = messageTopic;
    }

//    @MessageMapping("send")
//    void send(Flux<Message> messages) {
//        messages.map(message -> new OutboundMessage(exchange, "", SerializationUtils.serialize(message)))
//    }

    @ConnectMapping
    void onConnect(RSocketRequester requester)
    {
        Objects.requireNonNull(requester.rsocket()).onClose()
                .subscribe((close) -> log.info("disconnected" +  requester.rsocketClient()));

        log.info("connected" +  requester.rsocketClient());
    }

    @MessageMapping("send")
    void send( RSocketRequester requester, Message message) {
        this.reactiveTemplate.convertAndSend(messageTopic.getTopic(), message).subscribe();
    }

    @MessageMapping("receive")
    Flux<Message> receive() {
        List<ChannelTopic> topicList = Collections.singletonList(messageTopic);
        return reactiveMsgListenerContainer
                .receive(topicList, reactiveTemplate.getSerializationContext().getKeySerializationPair(),
                            reactiveTemplate.getSerializationContext().getValueSerializationPair())
                .map(ReactiveSubscription.Message::getMessage);
    }
}
