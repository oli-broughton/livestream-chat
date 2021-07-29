package io.disposechat.user;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class RedisUserRepository implements UserRepository {

    final ReactiveRedisOperations<String, User> userStore;

    public RedisUserRepository(ReactiveRedisOperations<String, User> userStore) {
        this.userStore = userStore;
    }

    @Override
    public Mono<User> add(String username) {
        return Mono.just(new User(UUID.randomUUID().toString(), username))
                .filterWhen(user -> userStore.opsForValue().setIfAbsent(user.getUsername(), user))
                .switchIfEmpty(Mono.error(new UsernameAlreadyUsedException()));
    }

    @Override
    public Mono<User> findByUsername(@NotNull String username) {
        return userStore.opsForValue().get(username);
    }

}

