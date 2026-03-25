package com.jobportal.application.controller;

import com.jobportal.application.dto.ChatMessageRequest;
import com.jobportal.application.model.ChatMessage;
import com.jobportal.application.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/{applicationId}")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable String applicationId,
            @Valid @RequestBody ChatMessageRequest req,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(messageService.send(applicationId, userId, req.getContent()));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String applicationId) {
        return ResponseEntity.ok(messageService.getHistory(applicationId));
    }
}
