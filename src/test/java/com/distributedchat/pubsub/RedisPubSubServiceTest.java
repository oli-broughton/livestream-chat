package com.distributedchat.pubsub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RedisPubSubServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReactiveRedisTemplate<String, Message> reactiveRedisTemplate;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer;

    @Mock
    private RedisSerializationContext.SerializationPair<String> stringSerializationPair;

    @Mock
    private RedisSerializationContext.SerializationPair<Message> messageSerializationPair;

    private PubSubService messagingService;

    @BeforeEach
    void setup(){
        messagingService = new RedisPubSubService(reactiveRedisTemplate, reactiveRedisMessageListenerContainer);
    }

    @Test
    void send() {
        var sendChannel = "broadcast";
        var testMessage = new Message("testuser", "test message");

        Mockito.when(reactiveRedisTemplate.convertAndSend(
                ArgumentMatchers.eq(sendChannel),
                ArgumentMatchers.eq(testMessage))).thenReturn(Mono.just(1L));

        StepVerifier.create(messagingService.publish(testMessage)).verifyComplete();
    }

    @Test
    void receive() {

        Mockito.when(reactiveRedisTemplate.getSerializationContext().getKeySerializationPair()).thenReturn(stringSerializationPair);
        Mockito.when(reactiveRedisTemplate.getSerializationContext().getValueSerializationPair()).thenReturn(messageSerializationPair);

        var testMessage = new Message("testuser", "test message");

        Mockito.when(reactiveRedisMessageListenerContainer.receive(
                ArgumentMatchers.<Iterable<ChannelTopic>>any(),
                ArgumentMatchers.eq(stringSerializationPair),
                ArgumentMatchers.eq(messageSerializationPair))
        ).thenReturn(Flux.just(new ReactiveSubscription.ChannelMessage<>("broadcast", testMessage)));

        var messages = messagingService.subscribe();

        StepVerifier.create(messages).expectNext(testMessage).thenCancel().verify();
    }
}