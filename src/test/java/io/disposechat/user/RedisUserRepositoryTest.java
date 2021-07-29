package io.disposechat.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisUserRepositoryTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    ReactiveRedisOperations<String, User> userStoreOperations;

    UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository = new RedisUserRepository(userStoreOperations);
    }

    @Test
    void addUsernameAvailable() {
        var username = "username";

        when(userStoreOperations
                .opsForValue()
                .setIfAbsent(eq(username), any(User.class)))
                .thenReturn(Mono.just(true));


        StepVerifier
                .create(userRepository.add(username))
                .consumeNextWith(user ->
                        Assertions.assertEquals(username, user.getUsername()))
                .verifyComplete();
    }

    @Test
    void addUsernameUnavailable() {
        var username = "username";

        when(userStoreOperations
                .opsForValue()
                .setIfAbsent(eq(username), any(User.class)))
                .thenReturn(Mono.just(false));

        StepVerifier
                .create(userRepository.add(username))
                .verifyComplete();
    }

    @Test
    void addIdNotEmpty() {
        when(userStoreOperations
                .opsForValue()
                .setIfAbsent(anyString(), any(User.class)))
                .thenReturn(Mono.just(true));

        StepVerifier
                .create(userRepository.add("username"))
                .consumeNextWith(user ->
                        Assertions.assertFalse(user.getId().isEmpty()))
                .verifyComplete();

    }


    @Test
    void findByIdFound() {
        var user = new User("id", "username");

        when(userStoreOperations.opsForValue().get(user.getUsername())).thenReturn(Mono.just(user));

        StepVerifier
                .create(userRepository.findByUsername(user.getUsername()))
                .expectNext(user)
                .verifyComplete();

    }

    @Test
    void findByIdNotFound() {
        var user = new User("id", "username");

        when(userStoreOperations.opsForValue().get(user.getId())).thenReturn(Mono.empty());

        StepVerifier
                .create(userRepository.findByUsername(user.getId()))
                .verifyComplete();

    }
}