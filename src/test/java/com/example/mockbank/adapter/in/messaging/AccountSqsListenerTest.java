package com.example.mockbank.adapter.in.messaging;

import com.example.mockbank.application.dto.AccountCreateRequest;
import com.example.mockbank.application.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import software.amazon.awssdk.services.sqs.model.Message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountSqsListenerTest {

    @Mock
    private AccountService accountService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private software.amazon.awssdk.services.sqs.SqsClient sqsClient;

    @InjectMocks
    private AccountSqsListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // queueUrl @Value 주입 문제는 ReflectionTestUtils로 우회 가능 (Spring Test 있을 때)
        // org.springframework.test.util.ReflectionTestUtils.setField(listener, "queueUrl", "mock-url");
    }

    @Test
    @DisplayName("정상 메시지 파싱 및 계좌 생성")
    void handleMessage_success() throws Exception {
        // given
        String json = "{\"userId\":10,\"userName\":\"테스터10\"}";
        Message message = Message.builder().body(json).build();

        // ObjectMapper.readTree(json) → JsonNode
        var jsonNode = new ObjectMapper().readTree(json); // 실제 ObjectMapper로 만듦(테스트용)
        when(objectMapper.readTree(json)).thenReturn(jsonNode);

        // when
        listener.handleMessage(message);

        // then
        verify(accountService).createAccount(any(AccountCreateRequest.class));
    }

    @Test
    @DisplayName("SNS 래핑 메시지 파싱")
    void handleMessage_snsWrappedMessage() throws Exception {
        // given
        String wrapped = "{\"Type\":\"Notification\",\"Message\":\"{\\\"userId\\\":20,\\\"userName\\\":\\\"테스터20\\\"}\"}";
        Message message = Message.builder().body(wrapped).build();

        var outerNode = new ObjectMapper().readTree(wrapped);
        var innerJson = outerNode.get("Message").asText();
        var innerNode = new ObjectMapper().readTree(innerJson);

        when(objectMapper.readTree(wrapped)).thenReturn(outerNode);
        when(objectMapper.readTree(innerJson)).thenReturn(innerNode);

        // when
        listener.handleMessage(message);

        // then
        verify(accountService).createAccount(any(AccountCreateRequest.class));
    }

    @Test
    @DisplayName("userName 없는 메시지 파싱")
    void handleMessage_noUserName() throws Exception {
        String json = "{\"userId\":11}";
        Message message = Message.builder().body(json).build();
        var jsonNode = new ObjectMapper().readTree(json);

        when(objectMapper.readTree(json)).thenReturn(jsonNode);

        listener.handleMessage(message);

        // then: userName은 null로 createAccount 호출됨
        ArgumentCaptor<AccountCreateRequest> captor = ArgumentCaptor.forClass(AccountCreateRequest.class);
        verify(accountService).createAccount(captor.capture());
        assert captor.getValue().getUserName() == null;
    }

    @Test
    @DisplayName("파싱 에러 시 예외 처리")
    void handleMessage_jsonParseError() throws Exception {
        // given
        Message message = Message.builder().body("invalid-json").build();
        when(objectMapper.readTree(any(String.class))).thenThrow(new RuntimeException("파싱에러"));

        // when/then: 예외 발생해도 서비스 죽지 않음, 로그만 남음
        listener.handleMessage(message);
        verify(accountService, never()).createAccount(any());
    }
}

