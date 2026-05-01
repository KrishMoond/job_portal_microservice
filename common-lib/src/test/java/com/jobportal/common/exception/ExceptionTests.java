package com.jobportal.common.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTests {

    @Test
    void badRequestException_shouldHoldMessage() {
        BadRequestException ex = new BadRequestException("Bad request");
        assertThat(ex.getMessage()).isEqualTo("Bad request");
    }

    @Test
    void resourceNotFoundException_shouldHoldMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        assertThat(ex.getMessage()).isEqualTo("Not found");
    }

    @Test
    void forbiddenException_shouldHoldMessage() {
        ForbiddenException ex = new ForbiddenException("Forbidden");
        assertThat(ex.getMessage()).isEqualTo("Forbidden");
    }
}
