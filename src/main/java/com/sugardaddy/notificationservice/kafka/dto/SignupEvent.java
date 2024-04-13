package com.sugardaddy.notificationservice.kafka.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SignupEvent implements Serializable {
    private String email;
    private String fullName;
    private LocalDateTime signupDateTime;
}