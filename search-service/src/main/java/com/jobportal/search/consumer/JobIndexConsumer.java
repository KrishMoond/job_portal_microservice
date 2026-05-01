package com.jobportal.search.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.common.events.JobClosedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import com.jobportal.search.model.JobSearchRecord;
import com.jobportal.search.repository.JobSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class JobIndexConsumer {

    private static final Logger log = LoggerFactory.getLogger(JobIndexConsumer.class);

    private final JobSearchRepository jobSearchRepository;
    private final ObjectMapper objectMapper;

    public JobIndexConsumer(JobSearchRepository jobSearchRepository, ObjectMapper objectMapper) {
        this.jobSearchRepository = jobSearchRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "job.created.search.queue")
    public void onJobCreated(String payload) {
        try {
            JobCreatedEvent event = objectMapper.readValue(payload, JobCreatedEvent.class);
            log.info("Indexing/re-indexing job: {}", event.getJobId());

            // Upsert: update existing record if present (handles reopen), insert if new
            JobSearchRecord record = jobSearchRepository.findByJobId(event.getJobId())
                .orElse(new JobSearchRecord());

            record.setJobId(event.getJobId());
            record.setTitle(event.getTitle());
            record.setCompany(event.getCompany());
            record.setLocation(event.getLocation());
            record.setSalary(event.getSalary());
            record.setDescription(event.getDescription());
            record.setRecruiterId(event.getRecruiterId());
            record.setCategory(event.getCategory());
            record.setStatus("OPEN");
            jobSearchRepository.save(record);
        } catch (Exception e) {
            log.error("[AMQP-CONSUMER] Failed to process JobCreatedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "job.closed.search.queue")
    public void onJobClosed(String payload) {
        try {
            JobClosedEvent event = objectMapper.readValue(payload, JobClosedEvent.class);
            log.info("Marking job as closed in index: {}", event.getJobId());
            jobSearchRepository.findByJobId(event.getJobId()).ifPresent(record -> {
                record.setStatus("CLOSED");
                jobSearchRepository.save(record);
            });
        } catch (Exception e) {
            log.error("[AMQP-CONSUMER] Failed to process JobClosedEvent: {}", e.getMessage(), e);
        }
    }
}
