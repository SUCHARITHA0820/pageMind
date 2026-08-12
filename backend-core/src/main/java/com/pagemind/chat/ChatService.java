package com.pagemind.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pagemind.chat.dto.ChatRequest;
import com.pagemind.chat.dto.ChatResponse;
import com.pagemind.user.User;
import com.pagemind.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final RestTemplate restTemplate;
    private final ChatHistoryRepository chatHistoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${ai.agent.service.url:http://localhost:8000/recommend}")
    private String aiAgentServiceUrl = "http://localhost:8000/recommend";

    @Transactional
    public ChatResponse processChat(ChatRequest request, String authenticatedEmail) {
        User user = resolveUser(request.getUserId(), authenticatedEmail);

        String activeSessionId = StringUtils.hasText(request.getSessionId()) ? request.getSessionId() : request.getSession_id();

        Map<String, Object> payload = new HashMap<>();
        payload.put("message", request.getMessage());
        payload.put("prompt", request.getMessage());
        if (user != null) {
            payload.put("userId", user.getId());
            payload.put("user_id", user.getId());
        } else if (request.getUserId() != null) {
            payload.put("userId", request.getUserId());
            payload.put("user_id", request.getUserId());
        }
        if (StringUtils.hasText(activeSessionId)) {
            payload.put("sessionId", activeSessionId);
            payload.put("session_id", activeSessionId);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        String detectedGenre = null;
        Object recommendedBooks = null;
        String responseMessage = null;
        Map<String, Object> rawAgentResponse = null;
        String booksJsonStr = null;
        String returnedSessionId = activeSessionId;

        try {
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(aiAgentServiceUrl, entity, Map.class);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                rawAgentResponse = responseEntity.getBody();

                detectedGenre = extractString(rawAgentResponse, "detected_genre", "detectedGenre", "genre");
                recommendedBooks = extractObject(rawAgentResponse, "recommended_books", "recommendedBooks", "recommendations", "books");
                responseMessage = extractString(rawAgentResponse, "message", "response", "recommendation");
                
                String agentSessionId = extractString(rawAgentResponse, "session_id", "sessionId");
                if (StringUtils.hasText(agentSessionId)) {
                    returnedSessionId = agentSessionId;
                }

                if (recommendedBooks != null) {
                    booksJsonStr = objectMapper.writeValueAsString(recommendedBooks);
                } else {
                    booksJsonStr = objectMapper.writeValueAsString(rawAgentResponse);
                }
            }
        } catch (RestClientException e) {
            log.warn("AI Agent service at {} is unavailable or returned an error: {}", aiAgentServiceUrl, e.getMessage());
            responseMessage = "AI Agent Service is currently unavailable. Chat request logged.";
        } catch (Exception e) {
            log.error("Error processing response from AI Agent service: {}", e.getMessage(), e);
            responseMessage = "Error parsing AI Agent response. Chat request logged.";
        }

        Long historyId = null;
        if (user != null) {
            ChatHistory chatHistory = ChatHistory.builder()
                    .user(user)
                    .moodInput(request.getMessage())
                    .detectedGenre(detectedGenre)
                    .recommendedBooksJson(booksJsonStr)
                    .build();
            ChatHistory savedHistory = chatHistoryRepository.save(chatHistory);
            historyId = savedHistory.getId();
        }

        boolean success = rawAgentResponse != null;

        return ChatResponse.builder()
                .success(success)
                .message(responseMessage != null ? responseMessage : (success ? "Recommendation generated successfully." : "Service unavailable."))
                .detectedGenre(detectedGenre)
                .recommendedBooks(recommendedBooks)
                .rawAgentResponse(rawAgentResponse)
                .historyId(historyId)
                .sessionId(returnedSessionId)
                .build();
    }

    private User resolveUser(Long userId, String authenticatedEmail) {
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }
        if (StringUtils.hasText(authenticatedEmail)) {
            return userRepository.findByEmail(authenticatedEmail).orElse(null);
        }
        return null;
    }

    private String extractString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return map.get(key).toString();
            }
        }
        return null;
    }

    private Object extractObject(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key) && map.get(key) != null) {
                return map.get(key);
            }
        }
        return null;
    }
}
