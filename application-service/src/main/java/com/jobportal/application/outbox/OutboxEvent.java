package com.jobportal.application.outbox;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id private String id;
    @Column(nullable = false) private String eventType;
    @Column(nullable = false) private String destination;
    @Column(nullable = false, columnDefinition = "TEXT") private String payload;
    @Column(nullable = false) private boolean processed = false;
    private LocalDateTime createdAt;

    public OutboxEvent() {}

    public OutboxEvent(String eventType, String destination, String payload) {
        this.id = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.destination = destination;
        this.payload = payload;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getEventType() { return eventType; }
    public String getDestination() { return destination; }
    public String getPayload() { return payload; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
