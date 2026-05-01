package com.jobportal.common.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DtoTests {

    @Test
    void apiResponse_success_shouldSetFields() {
        ApiResponse<String> response = ApiResponse.success("Data", "Success message");
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("Data");
        assertThat(response.getMessage()).isEqualTo("Success message");
    }

    @Test
    void apiResponse_error_shouldSetFields() {
        ApiResponse<Object> response = ApiResponse.error("Error message");
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Error message");
        assertThat(response.getData()).isNull();
    }
}
