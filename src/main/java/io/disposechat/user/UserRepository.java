package io.disposechat.user;

import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> save(User user);

    Mono<User> findByUsername(String username);
}
