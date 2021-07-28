package io.disposechat.user;

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

        client.post().uri(uriBuilder ->
                        uriBuilder.path("/user")
                                .queryParam("name", "Dave")
                                .build())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(User.class);

    }
}
