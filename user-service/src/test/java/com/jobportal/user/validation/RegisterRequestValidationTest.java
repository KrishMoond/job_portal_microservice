package com.jobportal.user.validation;

import com.jobportal.user.dto.RegisterRequest;
import com.jobportal.user.model.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestValidationTest {

    static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private RegisterRequest valid() {
        RegisterRequest r = new RegisterRequest();
        r.setName("John Doe");
        r.setEmail("john@example.com");
        r.setPassword("Secret@123");
        r.setRole(User.Role.JOB_SEEKER);
        return r;
    }

    @Test
    void validRequest_noViolations() {
        assertThat(validator.validate(valid())).isEmpty();
    }

    @Test
    void name_tooShort_violation() {
        RegisterRequest r = valid();
        r.setName("J");
        Set<ConstraintViolation<RegisterRequest>> v = validator.validate(r);
        assertThat(v).anyMatch(c -> c.getPropertyPath().toString().equals("name"));
    }

    @Test
    void name_withNumbers_violation() {
        RegisterRequest r = valid();
        r.setName("John123");
        assertThat(validator.validate(r)).anyMatch(c -> c.getPropertyPath().toString().equals("name"));
    }

    @Test
    void password_noUppercase_violation() {
        RegisterRequest r = valid();
        r.setPassword("secret@123");
        assertThat(validator.validate(r)).anyMatch(c -> c.getPropertyPath().toString().equals("password"));
    }

    @Test
    void password_noSpecialChar_violation() {
        RegisterRequest r = valid();
        r.setPassword("Secret123");
        assertThat(validator.validate(r)).anyMatch(c -> c.getPropertyPath().toString().equals("password"));
    }

    @Test
    void password_tooShort_violation() {
        RegisterRequest r = valid();
        r.setPassword("S@1");
        assertThat(validator.validate(r)).anyMatch(c -> c.getPropertyPath().toString().equals("password"));
    }

    @Test
    void email_invalid_violation() {
        RegisterRequest r = valid();
        r.setEmail("not-an-email");
        assertThat(validator.validate(r)).anyMatch(c -> c.getPropertyPath().toString().equals("email"));
    }
}
