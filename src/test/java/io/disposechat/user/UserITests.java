package io.disposechat.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserITests {

    @Autowired
    WebTestClient client;

    @Test
    void addUser() {

        var user = new User("username");

        client.post().uri("/api/user")
                .bodyValue(user)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(User.class).value(addedUser -> Assertions.assertEquals(user.getUsername(), addedUser.getUsername()));

    }

    @Test
    void addUserNotAvailable() {
        var user = new User("username");

        client.post().uri("/api/user")
                .bodyValue(user)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(User.class).value(addedUser -> Assertions.assertEquals(user.getUsername(), addedUser.getUsername()));

        client.post().uri("/api/user")
                .bodyValue(user)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }
}
