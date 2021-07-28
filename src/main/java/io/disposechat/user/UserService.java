package io.disposechat.user;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    Mono<User> add(String name) {
    return repo.add(name);
    }
}
