package com.jobportal.search.consumer;
 
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.common.events.JobClosedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import com.jobportal.search.model.JobSearchRecord;
import com.jobportal.search.repository.JobSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.util.Optional;
 
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
 
@ExtendWith(MockitoExtension.class)
class JobIndexConsumerTest {
 
    @Mock private JobSearchRepository repository;
    private ObjectMapper objectMapper = new ObjectMapper();
    private JobIndexConsumer consumer;
 
    @BeforeEach
    void setUp() {
        consumer = new JobIndexConsumer(repository, objectMapper);
    }
 
    @Test
    void onJobCreated_shouldIndexRecord() throws Exception {
        JobCreatedEvent event = new JobCreatedEvent();
        event.setJobId("job-123");
        event.setTitle("Software Engineer");
        event.setCompany("TechCorp");
        event.setLocation("Remote");
        event.setSalary("100000");
        event.setDescription("Cool job");
        event.setRecruiterId("rec-1");
 
        String payload = objectMapper.writeValueAsString(event);
 
        consumer.onJobCreated(payload);
 
        verify(repository).save(argThat(record -> 
            record.getJobId().equals("job-123") && 
            record.getTitle().equals("Software Engineer") &&
            record.getStatus().equals("OPEN")
        ));
    }
 
    @Test
    void onJobClosed_shouldUpdateStatus() throws Exception {
        JobClosedEvent event = new JobClosedEvent();
        event.setJobId("job-123");
 
        JobSearchRecord existing = new JobSearchRecord();
        existing.setJobId("job-123");
        existing.setStatus("OPEN");
 
        String payload = objectMapper.writeValueAsString(event);
 
        when(repository.findByJobId("job-123")).thenReturn(Optional.of(existing));
 
        consumer.onJobClosed(payload);
 
        verify(repository).save(argThat(record -> 
            record.getJobId().equals("job-123") && 
            record.getStatus().equals("CLOSED")
        ));
    }
 
    @Test
    void onJobCreated_handleException() {
        consumer.onJobCreated("invalid json");
        verify(repository, never()).save(any());
    }
 
    @Test
    void onJobClosed_handleException() {
        consumer.onJobClosed("invalid json");
        verify(repository, never()).save(any());
    }
}
