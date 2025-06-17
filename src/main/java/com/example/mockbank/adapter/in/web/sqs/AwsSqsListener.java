package com.example.mockbank.adapter.in.sqs;

import com.example.mockbank.application.dto.AccountCreatedEvent;
import com.example.mockbank.application.service.AccountService;
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
                        AccountCreatedEvent event = objectMapper.readValue(message.body(), AccountCreatedEvent.class);
                        accountService.createAccountFromEvent(event);
                    } catch (Exception e) {
                        log.error("SQS 메시지 처리 실패", e);
                    }
                }
            }
        });
    }
}
