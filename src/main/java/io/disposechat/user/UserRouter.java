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

import static org.springframework.web.reactive.function.BodyExtractors.toMono;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration(proxyBeanMethods = false)
@EnableWebFlux
public class UserRouter {

    final UserHandler userHandler;

    public UserRouter(UserService userService) {
        this.userHandler = new UserHandler(userService);
    }

    @Bean
    RouterFunction<ServerResponse> routes() {
        return route(POST("/api/user"), userHandler::addUser);
    }

    private class UserHandler {

        final UserService userService;

        public UserHandler(UserService userService) {
            this.userService = userService;
        }

        public @NotNull Mono<ServerResponse> addUser(ServerRequest request) {
            Mono<User> requestedUser = request.bodyToMono(User.class);
            Mono<User> addedUser = requestedUser.flatMap(userService::add);

            return addedUser.flatMap(user ->
                            ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(user))
                    .onErrorResume(UsernameAlreadyFoundException.class, error ->
                            ServerResponse.status(HttpStatus.CONFLICT)
                                    .bodyValue("Username is already in use"));
        }
    }
}
