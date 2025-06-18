package com.example.mockbank.adapter.out.messaging;

import com.example.mockbank.application.dto.AccountCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsSnsSender {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.sns.notification-topic-arn}")
    private String topicArn;

    public void sendNotification(Long userId, String email, String nickname, String userName) {
        try {
            // 1. 이벤트 객체 생성
            AccountCreatedEvent event = new AccountCreatedEvent(
                    userId, email, nickname, userName, LocalDateTime.now()
            );

            // 2. JSON 직렬화
            String jsonMessage = objectMapper.writeValueAsString(event);

            // 3. MessageAttribute에 queueId 포함
            String queueId = String.format("%d|USER_NOTIFICATION|%s|Welcome!",
                    userId, LocalDateTime.now());

            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(jsonMessage)
                    .messageAttributes(Map.of(
                            "queueId", MessageAttributeValue.builder()
                                    .dataType("String")
                                    .stringValue(queueId)
                                    .build()
                    ))
                    .build();

            log.info("SNS 전송: queueId={}, json={}", queueId, jsonMessage);
            snsClient.publish(request);

        } catch (Exception e) {
            log.error("SNS 전송 실패", e);
        }
    }
}
