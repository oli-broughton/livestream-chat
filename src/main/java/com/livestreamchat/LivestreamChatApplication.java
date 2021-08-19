package com.livestreamchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class LivestreamChatApplication {
    public static void main(String[] args) {
        //https://github.com/rsocket/rsocket-java/issues/1018
        Hooks.onErrorDropped((throwable)->{});
        SpringApplication.run(LivestreamChatApplication.class, args);
    }

}
