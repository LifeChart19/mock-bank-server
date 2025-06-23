package com.example.mockbank.adapter.in.web.sqs;

import com.example.mockbank.application.dto.AccountCreatedEvent;
import com.example.mockbank.application.service.AccountService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsSqsListener {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final AccountService accountService;

    @Value("${cloud.aws.sqs.queue-url}")
    private String queueUrl;

    @PostConstruct
    public void startPolling() {
        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                var request = ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .waitTimeSeconds(20)
                        .maxNumberOfMessages(5)
                        .build();

                var messages = sqsClient.receiveMessage(request).messages();

                for (Message message : messages) {
                    try {
                        log.info("📨 SQS 수신: {}", message.body());

                        // 1. SQS → SNS 구조의 래핑 메시지 처리
                        JsonNode root = objectMapper.readTree(message.body());
                        String innerJson = root.get("Message").asText(); // 내부 JSON 문자열 꺼냄

                        // 2. 실제 비즈니스 DTO로 파싱
                        AccountCreatedEvent event = objectMapper.readValue(innerJson, AccountCreatedEvent.class);
                        accountService.createAccountFromEvent(event);

                    } catch (Exception e) {
                        log.warn("SQS 메시지 처리 실패 - 무시된 메시지: {}", message.body(), e);
                    }
                }
            }
        });
    }
}
