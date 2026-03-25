package com.jobportal.application.service;

import com.jobportal.application.model.ChatMessage;
import com.jobportal.application.repository.ApplicationRepository;
import com.jobportal.application.repository.ChatMessageRepository;
import com.jobportal.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ApplicationRepository applicationRepository;

    public MessageService(ChatMessageRepository chatMessageRepository,
                          ApplicationRepository applicationRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public ChatMessage send(String applicationId, String senderId, String content) {
        if (!applicationRepository.existsById(applicationId))
            throw new ResourceNotFoundException("Application not found: " + applicationId);
        ChatMessage msg = new ChatMessage();
        msg.setApplicationId(applicationId);
        msg.setSenderId(senderId);
        msg.setContent(content);
        return chatMessageRepository.save(msg);
    }

    public List<ChatMessage> getHistory(String applicationId) {
        return chatMessageRepository.findByApplicationIdOrderBySentAtAsc(applicationId);
    }
}
