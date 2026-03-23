package com.jobportal.search.consumer;

import com.jobportal.common.events.JobClosedEvent;
import com.jobportal.common.events.JobCreatedEvent;
import com.jobportal.search.model.JobSearchRecord;
import com.jobportal.search.repository.JobSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class JobIndexConsumer {

    private static final Logger log = LoggerFactory.getLogger(JobIndexConsumer.class);

    private final JobSearchRepository jobSearchRepository;

    public JobIndexConsumer(JobSearchRepository jobSearchRepository) {
        this.jobSearchRepository = jobSearchRepository;
    }

    @JmsListener(destination = "job.created.topic")
    public void onJobCreated(JobCreatedEvent event) {
        log.info("Indexing new job: {}", event.getJobId());
        JobSearchRecord record = new JobSearchRecord();
        record.setJobId(event.getJobId());
        record.setTitle(event.getTitle());
        record.setCompany(event.getCompany());
        record.setLocation(event.getLocation());
        record.setRecruiterId(event.getRecruiterId());
        record.setStatus("OPEN");
        jobSearchRepository.save(record);
    }

    @JmsListener(destination = "job.closed.topic")
    public void onJobClosed(JobClosedEvent event) {
        log.info("Marking job as closed in index: {}", event.getJobId());
        jobSearchRepository.findByJobId(event.getJobId()).ifPresent(record -> {
            record.setStatus("CLOSED");
            jobSearchRepository.save(record);
        });
    }
}
