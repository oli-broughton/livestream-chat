package io.disposechat.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.BodyExtractors.toMono;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
@EnableWebFlux
public class UserRoutesConfig {

    final UserService userService;

    public UserRoutesConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    RouterFunction<ServerResponse> userRoutes() {
        return route(POST("/api/user/"),
                request -> request.body(toMono(User.class))
                        .doOnNext(userService::add)
                        .then(ok().build()));
    }
}
