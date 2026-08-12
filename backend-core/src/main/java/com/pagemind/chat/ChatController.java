package com.pagemind.chat;

import com.pagemind.chat.dto.ChatRequest;
import com.pagemind.chat.dto.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request, Principal principal) {
        String authenticatedEmail = principal != null ? principal.getName() : null;
        ChatResponse response = chatService.processChat(request, authenticatedEmail);
        return ResponseEntity.ok(response);
    }
}
