package io.disposechat.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.eq;


@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = UserRouter.class)
@WebFluxTest
public class UserRouteTests {

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private UserService service;

    @Autowired
    UserRouter userRouter;

    WebTestClient client;

    @BeforeEach
    public void setUp() {
        client = WebTestClient.bindToRouterFunction(userRouter.routes()).build();
    }

    @Test
    void addUser() {

        var user = new User("username");

        Mockito.when(service.add(eq(user))).thenReturn(Mono.just(user));

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

        Mockito.when(service.add(eq(user))).thenReturn(Mono.error(new UsernameAlreadyFoundException()));

        client.post().uri("/api/user")
                .bodyValue(user)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT);
    }
}
