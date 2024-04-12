package com.sugardaddy.notificationservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotifierListener {

    @KafkaListener(topics = "signup", groupId = "notifier")
    public void listenGroupFoo(String message) {
        log.info("Received kafka event: {}", message);
    }
}