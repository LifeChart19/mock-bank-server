package com.example.mockbank.adapter.in.messaging;

import com.example.mockbank.application.dto.AccountCreateRequest;
import com.example.mockbank.application.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.time.Duration;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountSqsListener {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final AccountService accountService;
    private final StringRedisTemplate redisTemplate;

    @Value("${aws.url.sqs.account}")
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
                        String eventKey = "account-event:" + message.messageId();
                        try {
                            if (Boolean.TRUE.equals(redisTemplate.hasKey(eventKey))) {
                                log.info("[AccountSqsListener] 중복 메시지 수신됨: {}", eventKey);
                                continue;
                            }

                            handleMessage(message);

                            redisTemplate.opsForValue().set(eventKey, "processed", Duration.ofHours(1));

                            deleteMessage(message); // 성공 시 삭제
                        } catch (Exception e) {
                            log.error("계좌 생성 메시지 처리 실패: {}", e.getMessage(), e);
                            // deleteMessage 호출 안 함 → SQS가 재시도
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
            req.setUserName(event.has("userName") ? event.get("userName").asText() : null);
            req.setAccountNumber(generateAccountNumber());

            accountService.createAccount(req);

            log.info("[AccountSqsListener] 계좌 생성 완료 for userId={}", req.getUserId());
        } catch (Exception e) {
            log.error("메시지 파싱/계좌생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("메시지 처리 실패", e);
        }
    }

    private void deleteMessage(Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
    }

    private String generateAccountNumber() {
        String accountNumber;
        int tryCount = 0;
        final int maxTry = 10;

        do {
            accountNumber = randomAccountNumber(10);
            tryCount++;
        } while (accountService.existsByAccountNumber(accountNumber) && tryCount < maxTry);

        if (tryCount == maxTry) {
            throw new RuntimeException("계좌번호 생성 시도 초과 (중복)");
        }
        return accountNumber;
    }

    private String randomAccountNumber(int length) {
        StringBuilder sb = new StringBuilder();
        sb.append((int) (Math.random() * 9) + 1);
        for (int i = 1; i < length; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
}
