package com.jobportal.common.events;
 
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
 
class EventTests {
 
    @Test
    void testJobCreatedEvent() {
        JobCreatedEvent event = new JobCreatedEvent();
        event.setJobId("1");
        event.setTitle("Title");
        event.setCompany("Company");
        event.setLocation("Location");
        event.setSalary("100");
        event.setDescription("Desc");
        event.setRecruiterId("rec-1");
 
        assertThat(event.getJobId()).isEqualTo("1");
        assertThat(event.getTitle()).isEqualTo("Title");
        assertThat(event.getCompany()).isEqualTo("Company");
        assertThat(event.getLocation()).isEqualTo("Location");
        assertThat(event.getSalary()).isEqualTo("100");
        assertThat(event.getDescription()).isEqualTo("Desc");
        assertThat(event.getRecruiterId()).isEqualTo("rec-1");
    }
 
    @Test
    void testJobAppliedEvent() {
        JobAppliedEvent event = new JobAppliedEvent();
        event.setApplicationId("app1");
        event.setJobId("j1");
        event.setJobTitle("Title");
        event.setCandidateId("c1");
        event.setCandidateEmail("email");
 
        assertThat(event.getApplicationId()).isEqualTo("app1");
        assertThat(event.getJobId()).isEqualTo("j1");
        assertThat(event.getJobTitle()).isEqualTo("Title");
        assertThat(event.getCandidateId()).isEqualTo("c1");
        assertThat(event.getCandidateEmail()).isEqualTo("email");
    }
 
    @Test
    void testJobClosedEvent() {
        JobClosedEvent event = new JobClosedEvent();
        event.setJobId("j1");
        assertThat(event.getJobId()).isEqualTo("j1");
    }
 
    @Test
    void testResumeUploadedEvent() {
        ResumeUploadedEvent event = new ResumeUploadedEvent();
        event.setUserId("u1");
        event.setFileUrl("url");
        event.setResumeId("r1");
        assertThat(event.getUserId()).isEqualTo("u1");
        assertThat(event.getFileUrl()).isEqualTo("url");
        assertThat(event.getResumeId()).isEqualTo("r1");
    }
 
    @Test
    void testInterviewScheduledEvent() {
        InterviewScheduledEvent event = new InterviewScheduledEvent();
        event.setApplicationId("a1");
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        event.setScheduledAt(now);
        event.setMeetingLink("meet");
        event.setCandidateId("c1");
 
        assertThat(event.getApplicationId()).isEqualTo("a1");
        assertThat(event.getScheduledAt()).isEqualTo(now);
        assertThat(event.getMeetingLink()).isEqualTo("meet");
        assertThat(event.getCandidateId()).isEqualTo("c1");
    }
}
