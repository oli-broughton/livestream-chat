package io.disposechat;

import io.disposechat.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class ReactiveRedisTests {

    @Autowired
    ReactiveRedisOperations<String, User> userStore;

    @Test
    void generateRandomKey() {
        StepVerifier.create(userStore.randomKey()).consumeNextWith(System.out::println).verifyComplete();
    }

    @Test
    void addUser() {
        var user = new User("1", "username");
        var createUser = Mono.just(user)
                .filterWhen(u -> userStore.opsForValue().setIfAbsent(u.getUsername(), u));

        StepVerifier.create(createUser).expectNext(user).verifyComplete();
    }
}
