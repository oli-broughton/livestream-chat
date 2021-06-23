package io.disposechat.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("messaging.broadcastTopic")
    final String broadcastTopic = "";

    @Bean
    public RedisTemplate<String, Message> redisTemplate(RedisConnectionFactory connectionFactory) {
        final RedisTemplate<String, Message> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Message.class));
        return template;
    }

    @Bean
    MessageListenerAdapter messageListenerAdapter(MessageBroker messageBroker) {
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(messageBroker);
        messageListenerAdapter.setSerializer(new Jackson2JsonRedisSerializer<>(Message.class));
        return messageListenerAdapter;
    }


    @Bean
    RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory, MessageListenerAdapter adapter) {
        RedisMessageListenerContainer container
                = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(adapter, broadcastTopic());
        return container;
    }

    @Bean
    ChannelTopic broadcastTopic() {
        return new ChannelTopic(broadcastTopic);
    }

}