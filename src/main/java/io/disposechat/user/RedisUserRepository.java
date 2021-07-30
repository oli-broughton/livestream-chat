package io.disposechat.user;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class RedisUserRepository implements UserRepository {

    final ReactiveRedisOperations<String, User> userStore;

    public RedisUserRepository(ReactiveRedisOperations<String, User> userStore) {
        this.userStore = userStore;
    }

    @Override
    public Mono<User> save(User user) {
        return Mono.just(user)
                .filterWhen(u -> userStore.opsForValue().setIfAbsent(u.getUsername(), u))
                .switchIfEmpty(Mono.error(new UsernameAlreadyUsedException()));
    }

    @Override
    public Mono<User> findByUsername(@NotNull String username) {
        return userStore.opsForValue().get(username);
    }

}

