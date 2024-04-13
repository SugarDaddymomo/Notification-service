package com.sugardaddy.notificationservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sugardaddy.notificationservice.kafka.dto.SignupEvent;
import com.sugardaddy.notificationservice.model.User;
import com.sugardaddy.notificationservice.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import java.io.IOException;

@Component
@Slf4j
public class NotifierListener {

    @Autowired
    private Jackson2ObjectMapperBuilder objectMapperBuilder;

    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        objectMapper = objectMapperBuilder.build();
    }

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @KafkaListener(topics = "signup", groupId = "notifier")
    public void listenGroupFoo(String message) throws JsonProcessingException {
        log.info("Received kafka event: {}", message);
        SignupEvent signupEvent = objectMapper.readValue(message, SignupEvent.class);
        // use string template to send mail
        try {
            Resource resource = resourceLoader.getResource("classpath:/templates/welcome_email.stg");
            if (!resource.exists()) {
                log.error("Template file not found: {}", resource.getFilename());
                return;
            }
            STGroup group = new STGroupFile(resource.getFile().getAbsolutePath(), "UTF-8", '$', '$');
            ST template = group.getInstanceOf("welcome_email");
            if (template == null) {
                log.error("Template 'welcome_email' not found in file: {}", resource.getFilename());
                return;
            }
            template.add("userName", signupEvent.getFullName());
            template.add("userEmail", signupEvent.getEmail());
            String emailBody = template.render();
            log.debug("Rendered email body: {}", emailBody);
            System.out.println(emailBody);
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(emailBody, true);
            helper.setTo(signupEvent.getEmail());
            helper.setSubject("Welcome to our Development Circle!");
            helper.setFrom("ashutosh.yadav@dev.circle");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            log.error("Error sending mail with exception: ", ex);
        } catch (IOException e) {
            log.error("Error loading template file: {}", e.getMessage());
        }
    }
}