package io.disposechat;

import io.disposechat.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

@SpringBootTest
public class ReactiveRedisTests {

    @Autowired
    ReactiveRedisOperations<String, User> userStore;

    @Test
    void generateRandomKey(){
        StepVerifier.create(userStore.randomKey()).consumeNextWith(System.out::println).verifyComplete();
    }

    @Test
    void addUser(){

        var createUser = Mono.just(new User(UUID.randomUUID().toString(), "dave"))
                .flatMap(user -> userStore.opsForValue().setIfAbsent(user.getId(), user));

        StepVerifier.create(createUser).expectNext(true).verifyComplete();
    }
}
