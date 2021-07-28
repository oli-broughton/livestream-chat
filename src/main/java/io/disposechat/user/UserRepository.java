package io.disposechat.user;

import reactor.core.publisher.Mono;

public interface UserRepository{

    Mono<User> add(String name);
    Mono<User> findById(String Id);

}
