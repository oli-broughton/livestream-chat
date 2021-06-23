package io.disposechat.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisMessagePublisher implements MessagePublisher {

    @Value("${messaging.broadcastTopic}")
    final String broadcastTopic = "";

    final RedisTemplate<String, Message> redisTemplate;

    public RedisMessagePublisher(RedisTemplate<String, Message> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publishMessage(Message m){
        redisTemplate.convertAndSend(broadcastTopic, m);
    }
}
