package com.chomu.raspiaiagent.controller;

import com.chomu.raspiaiagent.service.ChatAgentService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatAgentService chatAgentService;

    public ChatController(ChatAgentService chatAgentService) {
        this.chatAgentService = chatAgentService;
    }

    public record ChatRequest(
            @NotBlank(message = "message는 필수입니다.") String message,
            String conversationId
    ) {}

    public record ChatResponse(String conversationId, String reply) {}

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String conversationId = (request.conversationId() != null)
                ? request.conversationId()
                : UUID.randomUUID().toString();

        String reply = chatAgentService.chat(conversationId, request.message());

        return ResponseEntity.ok(new ChatResponse(conversationId, reply));
    }
}