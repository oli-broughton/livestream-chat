package io.disposechat.user;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
public class User {
    private UUID uuid;
    private String username;

    public User() {
        this.uuid = UUID.randomUUID();
    }

    public User(String username) {
        this();
        this.username = username;
    }
}
