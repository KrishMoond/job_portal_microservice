package com.jobportal.search.model;
 
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
 
class ModelTests {
 
    @Test
    void testJobSearchRecord() {
        JobSearchRecord record = new JobSearchRecord();
        record.setId("id1");
        record.setJobId("job1");
        record.setTitle("title");
        record.setCompany("company");
        record.setLocation("location");
        record.setSalary("50000");
        record.setDescription("desc");
        record.setRecruiterId("rec-1");
        record.setStatus("OPEN");
 
        assertThat(record.getId()).isEqualTo("id1");
        assertThat(record.getJobId()).isEqualTo("job1");
        assertThat(record.getTitle()).isEqualTo("title");
        assertThat(record.getCompany()).isEqualTo("company");
        assertThat(record.getLocation()).isEqualTo("location");
        assertThat(record.getSalary()).isEqualTo("50000");
        assertThat(record.getDescription()).isEqualTo("desc");
        assertThat(record.getRecruiterId()).isEqualTo("rec-1");
        assertThat(record.getStatus()).isEqualTo("OPEN");
    }

    @Test
    void testPrePersist() throws Exception {
        JobSearchRecord record = new JobSearchRecord();
        // Since we can't easily trigger JPA lifecycle in unit test without EntityManager,
        // we'll at least test the method logic if it's accessible or assume it will be covered by Integration test.
        // Actually, we can just call it if it's protected/public.
        java.lang.reflect.Method method = JobSearchRecord.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(record);

        assertThat(record.getId()).isNotNull();
        assertThat(record.getStatus()).isEqualTo("OPEN");
        assertThat(record.getCreatedAt()).isNotNull();
    }
}
