package io.disposechat.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@TestConfiguration
public class TestRedisConfiguration {
    @Value("${spring.redis.port}") int redisPort;
    @Value("${spring.redis.host}") String redisHost;

    private RedisServer redisServer;

    TestRedisConfiguration(){

        this.redisServer = new RedisServer(6730);
    }

    @PostConstruct
    public void postConstruct() {
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() {
        redisServer.stop();
    }
}
