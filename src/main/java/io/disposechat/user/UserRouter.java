package io.disposechat.user;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration(proxyBeanMethods = false)
@EnableWebFlux
public class UserRouter {

    final UserHandler userHandler;

    public UserRouter(UserService userService, ChatTokenService chatTokenService) {
        this.userHandler = new UserHandler(userService, chatTokenService);
    }

    @Bean
    RouterFunction<ServerResponse> routes() {
        return route(POST("/api/user"), userHandler::addUser);
    }

    private class UserHandler {

        final UserService userService;
        final ChatTokenService chatTokenService;

        public UserHandler(UserService userService, ChatTokenService chatTokenService) {
            this.userService = userService;
            this.chatTokenService = chatTokenService;
        }

        public @NotNull Mono<ServerResponse> addUser(ServerRequest request) {
            Mono<User> requestedUser = request.bodyToMono(User.class);

            return requestedUser.
                    flatMap(userService::save).
                    map(chatTokenService::encodeToken)
                    .flatMap(token ->
                            ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(token))
                    .onErrorResume(UsernameAlreadyFoundException.class, error ->
                            ServerResponse.status(HttpStatus.CONFLICT)
                                    .bodyValue("Username is already in use"));

        }
    }
}
