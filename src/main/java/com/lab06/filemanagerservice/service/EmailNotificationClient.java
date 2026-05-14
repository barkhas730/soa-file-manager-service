package com.lab06.filemanagerservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailNotificationClient {

    private final RestTemplate restTemplate;
    private final String emailServiceUrl;
    private final String defaultToEmail;

    public EmailNotificationClient(
            @Value("${email.service.url:http://localhost:8084}") String emailServiceUrl,
            @Value("${email.default.to:}") String defaultToEmail) {
        this.restTemplate = new RestTemplate();
        this.emailServiceUrl = emailServiceUrl;
        this.defaultToEmail = defaultToEmail;
    }

    public void sendSuccess(String to, String userName, String fileName, String fileUrl) {
        String targetEmail = resolveEmail(to);
        if (targetEmail == null) {
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("to", targetEmail);
        body.put("userName", userName);
        body.put("fileName", fileName);
        body.put("fileUrl", fileUrl);
        post("/api/email/success", body);
    }

    public void sendFail(String to, String userName, String fileName, String reason) {
        String targetEmail = resolveEmail(to);
        if (targetEmail == null) {
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("to", targetEmail);
        body.put("userName", userName);
        body.put("fileName", fileName == null ? "unknown-file" : fileName);
        body.put("reason", reason);
        post("/api/email/fail", body);
    }

    private void post(String path, Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(emailServiceUrl + path, new HttpEntity<>(body, headers), String.class);
    }

    private String resolveEmail(String to) {
        if (to != null && !to.isBlank()) {
            return to;
        }
        if (defaultToEmail != null && !defaultToEmail.isBlank()) {
            return defaultToEmail;
        }
        return null;
    }
}

