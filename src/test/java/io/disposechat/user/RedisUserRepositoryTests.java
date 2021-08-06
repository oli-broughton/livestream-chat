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
class RedisUserRepositoryTests {

    @Mock(answer = RETURNS_DEEP_STUBS)
    ReactiveRedisOperations<String, User> userStoreOperations;

    UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository = new RedisUserRepository(userStoreOperations);
    }

    @Test
    void addUsernameAvailable() {
        var newUser = new User("username");

        when(userStoreOperations
                .opsForValue()
                .setIfAbsent(eq(newUser.getUsername()), any(User.class)))
                .thenReturn(Mono.just(true));


        StepVerifier
                .create(userRepository.save(newUser))
                .consumeNextWith(savedUser ->
                        Assertions.assertEquals(savedUser, newUser))
                .verifyComplete();
    }

    @Test
    void addUsernameUnavailable() {
        var newUser = new User("username");

        when(userStoreOperations
                .opsForValue()
                .setIfAbsent(eq(newUser.getUsername()), any(User.class)))
                .thenReturn(Mono.just(false));


        StepVerifier
                .create(userRepository.save(newUser))
                .expectError(UsernameAlreadyFoundException.class)
                .verify();
    }

    @Test
    void findByUserFound() {
        var user = new User( "username");

        when(userStoreOperations.opsForValue().get(user.getUsername())).thenReturn(Mono.just(user));

        StepVerifier
                .create(userRepository.findByUsername(user.getUsername()))
                .expectNext(user)
                .verifyComplete();

    }

    @Test
    void findByIdNotFound() {
        var user = new User("username");

        when(userStoreOperations.opsForValue().get(user.getUsername())).thenReturn(Mono.empty());

        StepVerifier
                .create(userRepository.findByUsername(user.getUsername()))
                .verifyComplete();

    }
}