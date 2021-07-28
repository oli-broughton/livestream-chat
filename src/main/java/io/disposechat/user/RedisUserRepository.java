package io.disposechat.user;

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
    public Mono<User> add(String name) {
        return Mono.empty();
    }

    @Override
    public Mono<User> findById(String id) {
        return Mono.empty();
    }

}
