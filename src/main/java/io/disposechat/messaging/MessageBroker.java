package io.disposechat.messaging;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageBroker implements MessageSubscriber {

    final SimpMessagingTemplate simpMessagingTemplate;

    public MessageBroker(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void handleMessage(Message m) {
        simpMessagingTemplate.convertAndSend("/broadcast", m);
    }
}
