package io.disposechat.messaging;

public interface MessageSubscriber {
    void handleMessage(Message m);
}
