package io.disposechat.messaging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class MessagingApplication {
    public static void main(String[] args) {
        //https://github.com/rsocket/rsocket-java/issues/1018
        Hooks.onErrorDropped((throwable)->{});
        SpringApplication.run(MessagingApplication.class, args);
    }

}
