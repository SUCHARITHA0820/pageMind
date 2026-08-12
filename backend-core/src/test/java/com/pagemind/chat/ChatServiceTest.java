package com.pagemind.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagemind.chat.dto.ChatRequest;
import com.pagemind.chat.dto.ChatResponse;
import com.pagemind.user.User;
import com.pagemind.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ChatHistoryRepository chatHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ChatService chatService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("Bob")
                .email("bob@example.com")
                .build();
    }

    @Test
    void processChat_success_forwardsToAgentAndSavesHistory() {
        ChatRequest request = ChatRequest.builder()
                .message("I want a thriller book")
                .userId(1L)
                .build();

        Map<String, Object> agentResponseMap = new HashMap<>();
        agentResponseMap.put("detected_genre", "Thriller");
        agentResponseMap.put("recommended_books", Collections.singletonList("The Da Vinci Code"));
        agentResponseMap.put("message", "Here are your book recommendations.");

        ResponseEntity<Map> mockEntity = new ResponseEntity<>(agentResponseMap, HttpStatus.OK);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(mockEntity);

        ChatHistory savedHistory = ChatHistory.builder()
                .id(50L)
                .user(sampleUser)
                .moodInput("I want a thriller book")
                .detectedGenre("Thriller")
                .build();

        when(chatHistoryRepository.save(any(ChatHistory.class))).thenReturn(savedHistory);

        ChatResponse response = chatService.processChat(request, "bob@example.com");

        assertTrue(response.isSuccess());
        assertEquals("Thriller", response.getDetectedGenre());
        assertEquals(50L, response.getHistoryId());
        verify(chatHistoryRepository, times(1)).save(any(ChatHistory.class));
    }

    @Test
    void processChat_whenAgentDown_handlesFallbackAndSavesHistory() {
        ChatRequest request = ChatRequest.builder()
                .message("Looking for mystery")
                .userId(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("Connection refused"));

        ChatHistory savedHistory = ChatHistory.builder()
                .id(51L)
                .user(sampleUser)
                .moodInput("Looking for mystery")
                .build();

        when(chatHistoryRepository.save(any(ChatHistory.class))).thenReturn(savedHistory);

        ChatResponse response = chatService.processChat(request, "bob@example.com");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("unavailable"));
        assertEquals(51L, response.getHistoryId());
        verify(chatHistoryRepository, times(1)).save(any(ChatHistory.class));
    }
}
