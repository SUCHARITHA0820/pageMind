package com.pagemind.chat;

import com.pagemind.chat.dto.ChatRequest;
import com.pagemind.chat.dto.ChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private Principal principal;

    @InjectMocks
    private ChatController chatController;

    @Test
    void chat_returnsOkResponse() {
        ChatRequest request = ChatRequest.builder()
                .message("Recommend a sci-fi book")
                .userId(1L)
                .build();

        ChatResponse mockResponse = ChatResponse.builder()
                .success(true)
                .message("Recommendations ready")
                .detectedGenre("Sci-Fi")
                .historyId(10L)
                .build();

        when(principal.getName()).thenReturn("bob@example.com");
        when(chatService.processChat(any(ChatRequest.class), eq("bob@example.com"))).thenReturn(mockResponse);

        ResponseEntity<ChatResponse> response = chatController.chat(request, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Sci-Fi", response.getBody().getDetectedGenre());
    }
}
