package com.jobportal.job.validation;

import com.jobportal.job.dto.JobRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JobRequestValidationTest {

    static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private JobRequest valid() {
        JobRequest r = new JobRequest();
        r.setTitle("Backend Developer");
        r.setCompanyId("company-1");
        r.setLocation("New York, NY");
        r.setSalary("$80,000/yr");
        r.setDescription("Java role");
        return r;
    }

    @Test
    void validRequest_noViolations() {
        assertThat(validator.validate(valid())).isEmpty();
    }

    @Test
    void location_tooShort_violation() {
        JobRequest r = valid();
        r.setLocation("X");
        assertThat(validator.validate(r)).anyMatch(c -> c.getPropertyPath().toString().equals("location"));
    }

    @Test
    void location_withNumbers_violation() {
        JobRequest r = valid();
        r.setLocation("New York 123");
        assertThat(validator.validate(r)).anyMatch(c -> c.getPropertyPath().toString().equals("location"));
    }

    @Test
    void salary_invalidFormat_violation() {
        JobRequest r = valid();
        r.setSalary("fifty thousand");
        assertThat(validator.validate(r)).anyMatch(c -> c.getPropertyPath().toString().equals("salary"));
    }

    @Test
    void salary_validFormats_noViolations() {
        for (String salary : new String[]{"$50,000/yr", "$80,000-$100,000/yr", "$25/hr", "$5,000/mo"}) {
            JobRequest r = valid();
            r.setSalary(salary);
            assertThat(validator.validate(r))
                .as("Expected no violations for salary: " + salary)
                .noneMatch(c -> c.getPropertyPath().toString().equals("salary"));
        }
    }

    @Test
    void title_blank_violation() {
        JobRequest r = valid();
        r.setTitle("");
        assertThat(validator.validate(r)).anyMatch(c -> c.getPropertyPath().toString().equals("title"));
    }

    @Test
    void salary_null_noViolation() {
        JobRequest r = valid();
        r.setSalary(null);
        assertThat(validator.validate(r)).noneMatch(c -> c.getPropertyPath().toString().equals("salary"));
    }
}
