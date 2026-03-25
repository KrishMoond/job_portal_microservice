package com.jobportal.application.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatMessageRequest {

    @NotBlank private String content;

    public ChatMessageRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
