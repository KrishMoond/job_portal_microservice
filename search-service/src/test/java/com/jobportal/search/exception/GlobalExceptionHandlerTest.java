package com.jobportal.search.exception;
 
import com.jobportal.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
 
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
@WebMvcTest(GlobalExceptionHandler.class)
@ContextConfiguration(classes = {GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {
 
    @Autowired private MockMvc mockMvc;
 
    @RestController
    static class TestController {
        @GetMapping("/test/notfound")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Resource not found");
        }
 
        @GetMapping("/test/error")
        public void throwError() {
            throw new RuntimeException("General error");
        }
    }
 
    @Test
    void handleNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/test/notfound"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Resource not found"));
    }
 
    @Test
    void handleGeneral_shouldReturn500() throws Exception {
        mockMvc.perform(get("/test/error"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}
