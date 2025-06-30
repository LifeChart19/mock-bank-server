package com.example.mockbank.adapter.in.messaging;

import com.example.mockbank.application.dto.AccountCreateRequest;
import com.example.mockbank.application.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.model.Message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountSqsListenerTest {

    @Mock
    private AccountService accountService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private software.amazon.awssdk.services.sqs.SqsClient sqsClient;

    @InjectMocks
    private AccountSqsListener listener;

    @Test
    @DisplayName("정상 메시지 파싱 및 계좌 생성")
    void handleMessage_success() throws Exception {
        // given
        String json = "{\"userId\":10,\"userName\":\"테스터10\"}";
        Message message = Message.builder().body(json).build();

        var jsonNode = new ObjectMapper().readTree(json);
        given(objectMapper.readTree(json)).willReturn(jsonNode);

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

        given(objectMapper.readTree(wrapped)).willReturn(outerNode);
        given(objectMapper.readTree(innerJson)).willReturn(innerNode);

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

        given(objectMapper.readTree(json)).willReturn(jsonNode);

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
        given(objectMapper.readTree(any(String.class))).willThrow(new RuntimeException("파싱에러"));

        // when/then: 예외 발생해도 서비스 죽지 않음, 로그만 남음
        listener.handleMessage(message);
        verify(accountService, never()).createAccount(any());
    }
}
