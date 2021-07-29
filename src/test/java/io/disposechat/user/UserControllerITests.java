package io.disposechat.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerITests {

    @Autowired
    WebTestClient client;

    @Test
    void addUser() {

        var username = "username";

        client.post().uri(uriBuilder ->
                        uriBuilder.path("/api/user")
                                .queryParam("name", username)
                                .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(User.class).value(user -> Assertions.assertEquals(username, user.getUsername()));

    }

    @Test
    void addUserNotAvailable() {

        var username = "username";

        client.post().uri(uriBuilder ->
                        uriBuilder.path("/api/user")
                                .queryParam("name", username)
                                .build())
                .exchange()
                .expectStatus()
                .isOk()
                        .expectBody(User.class).value(user -> Assertions.assertEquals(username, user.getUsername()));

        client.post().uri(uriBuilder ->
                        uriBuilder.path("/api/user")
                                .queryParam("name", username)
                                .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody().isEmpty();
    }
}
