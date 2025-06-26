package com.example.mockbank.adapter.in.messaging;

import com.example.mockbank.application.dto.AccountCreateRequest;
import com.example.mockbank.application.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountSqsListener {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final AccountService accountService;

    @Value("${aws.url.sqs.notification}")
    private String queueUrl; // application.properties에 있는 SQS 큐 URL

    @PostConstruct
    public void startPolling() {
        log.info("[AccountSqsListener] SQS Listener 시작: {}", queueUrl);

        Executors.newSingleThreadExecutor().submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    var response = sqsClient.receiveMessage(
                            ReceiveMessageRequest.builder()
                                    .queueUrl(queueUrl)
                                    .waitTimeSeconds(20)
                                    .maxNumberOfMessages(5)
                                    .build()
                    );

                    for (Message message : response.messages()) {
                        try {
                            handleMessage(message);
                        } catch (Exception e) {
                            log.error("계좌 생성 메시지 처리 실패: {}", e.getMessage(), e);
                        } finally {
                            deleteMessage(message);
                        }
                    }
                } catch (Exception e) {
                    log.error("SQS 폴링 에러: {}", e.getMessage(), e);
                    if (e.getMessage().contains("Connection pool shut down")) {
                        break;
                    }
                }
            }
        });
    }

    void handleMessage(Message message) {
        log.info("[AccountSqsListener] 수신 메시지: {}", message.body());
        try {
            var tree = objectMapper.readTree(message.body());
            String eventStr = tree.has("Message") ? tree.get("Message").asText() : message.body();

            var event = objectMapper.readTree(eventStr);

            AccountCreateRequest req = new AccountCreateRequest();
            req.setUserId(event.get("userId").asLong());
            req.setUserName(event.has("userName") ? event.get("userName").asText() : null); // <- userName 세팅!
            req.setAccountNumber(generateAccountNumber());

            // 계좌 생성
            accountService.createAccount(req);

            log.info("[AccountSqsListener] 계좌 생성 완료 for userId={}", req.getUserId());
        } catch (Exception e) {
            log.error("메시지 파싱/계좌생성 실패: {}", e.getMessage(), e);
        }
    }

    private void deleteMessage(Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
    }

    // 간단한 계좌번호 생성 로직 (랜덤 등)
    private String generateAccountNumber() {
        return String.valueOf(System.currentTimeMillis()).substring(4, 13);
    }
}
