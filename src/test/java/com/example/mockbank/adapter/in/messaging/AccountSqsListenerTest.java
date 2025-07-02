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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountSqsListenerTest {

    @Mock private AccountService accountService;
    @Mock private ObjectMapper objectMapper;
    @Mock private software.amazon.awssdk.services.sqs.SqsClient sqsClient;

    @InjectMocks private AccountSqsListener listener;

    @Test
    @DisplayName("정상 메시지 파싱 및 계좌 생성 - salary 없음")
    void handleMessage_success() throws Exception {
        String json = "{\"userId\":10,\"userName\":\"테스터10\"}";
        Message message = Message.builder().body(json).build();

        var jsonNode = new ObjectMapper().readTree(json);
        given(objectMapper.readTree(json)).willReturn(jsonNode);
        given(accountService.existsByAccountNumber(anyString())).willReturn(false);

        listener.handleMessage(message);

        ArgumentCaptor<AccountCreateRequest> captor = ArgumentCaptor.forClass(AccountCreateRequest.class);
        verify(accountService).createAccount(captor.capture());
        assertEquals(10L, captor.getValue().getUserId());
        assertEquals("테스터10", captor.getValue().getUserName());
        assertNull(captor.getValue().getSalary());
        assertNotNull(captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("정상 메시지 파싱 및 계좌 생성 - salary 있음")
    void handleMessage_salaryExists() throws Exception {
        String json = "{\"userId\":15,\"userName\":\"테스터15\",\"salary\":12345.67}";
        Message message = Message.builder().body(json).build();

        var jsonNode = new ObjectMapper().readTree(json);
        given(objectMapper.readTree(json)).willReturn(jsonNode);
        given(accountService.existsByAccountNumber(anyString())).willReturn(false);

        listener.handleMessage(message);

        ArgumentCaptor<AccountCreateRequest> captor = ArgumentCaptor.forClass(AccountCreateRequest.class);
        verify(accountService).createAccount(captor.capture());
        assertEquals(new BigDecimal("12345.67"), captor.getValue().getSalary());
    }

    @Test
    @DisplayName("SNS 래핑 메시지 파싱")
    void handleMessage_snsWrappedMessage() throws Exception {
        String wrapped = "{\"Type\":\"Notification\",\"Message\":\"{\\\"userId\\\":20,\\\"userName\\\":\\\"테스터20\\\"}\"}";
        Message message = Message.builder().body(wrapped).build();

        var outerNode = new ObjectMapper().readTree(wrapped);
        var innerJson = outerNode.get("Message").asText();
        var innerNode = new ObjectMapper().readTree(innerJson);

        given(objectMapper.readTree(wrapped)).willReturn(outerNode);
        given(objectMapper.readTree(innerJson)).willReturn(innerNode);
        given(accountService.existsByAccountNumber(anyString())).willReturn(false);

        listener.handleMessage(message);

        verify(accountService).createAccount(any(AccountCreateRequest.class));
    }

    @Test
    @DisplayName("userName 없는 메시지 파싱")
    void handleMessage_noUserName() throws Exception {
        String json = "{\"userId\":11}";
        Message message = Message.builder().body(json).build();
        var jsonNode = new ObjectMapper().readTree(json);

        given(objectMapper.readTree(json)).willReturn(jsonNode);
        given(accountService.existsByAccountNumber(anyString())).willReturn(false);

        listener.handleMessage(message);

        ArgumentCaptor<AccountCreateRequest> captor = ArgumentCaptor.forClass(AccountCreateRequest.class);
        verify(accountService).createAccount(captor.capture());
        assertNull(captor.getValue().getUserName());
    }

    @Test
    @DisplayName("파싱 에러 시 예외 처리")
    void handleMessage_jsonParseError() throws Exception {
        Message message = Message.builder().body("invalid-json").build();
        given(objectMapper.readTree(any(String.class))).willThrow(new RuntimeException("파싱에러"));

        assertThrows(RuntimeException.class, () -> listener.handleMessage(message));
        verify(accountService, never()).createAccount(any());
    }
}

