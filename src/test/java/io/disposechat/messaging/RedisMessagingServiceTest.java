package io.disposechat.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RedisMessagingServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ReactiveRedisTemplate<String, Message> reactiveRedisTemplate;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer;

    @Mock
    RedisSerializationContext.SerializationPair<String> stringSerializationPair;

    @Mock
    RedisSerializationContext.SerializationPair<Message> messageSerializationPair;

    MessagingService messagingService;

    @BeforeEach
    void setup(){
        Mockito.when(reactiveRedisTemplate.getSerializationContext().getKeySerializationPair()).thenReturn(stringSerializationPair);
        Mockito.when(reactiveRedisTemplate.getSerializationContext().getValueSerializationPair()).thenReturn(messageSerializationPair);

        messagingService = new RedisMessagingService(reactiveRedisTemplate, reactiveRedisMessageListenerContainer);
    }

    @Test
    void sendSuccessful() {
        var sendChannel = "broadcast";
        var testMessage = new Message("testuser", "test message");

        Mockito.when(reactiveRedisTemplate.convertAndSend(
                ArgumentMatchers.eq(sendChannel),
                ArgumentMatchers.eq(testMessage))).thenReturn(Mono.just(1L));

        var response = messagingService.send(testMessage);

        StepVerifier.create(response).expectNext(true).verifyComplete();
    }

    @Test
    void receive() {
        var testMessage = new Message("testuser", "test message");

        Mockito.when(reactiveRedisMessageListenerContainer.receive(
                ArgumentMatchers.<Iterable<ChannelTopic>>any(),
                ArgumentMatchers.< RedisSerializationContext.SerializationPair<String>>any(),
                ArgumentMatchers.< RedisSerializationContext.SerializationPair<Message>>any())
        ).thenReturn(Flux.just(new ReactiveSubscription.ChannelMessage<>("broadcast", testMessage)));

        var messages = messagingService.receive();

        StepVerifier.create(messages).expectNext(testMessage).thenCancel().verify();
    }
}