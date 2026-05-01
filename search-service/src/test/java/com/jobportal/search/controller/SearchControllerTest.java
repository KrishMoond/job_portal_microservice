package com.jobportal.search.controller;
 
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.search.model.JobSearchRecord;
import com.jobportal.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
 
import java.util.Collections;
 
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
@WebMvcTest(SearchController.class)
class SearchControllerTest {
 
    @Autowired private MockMvc mockMvc;
    @MockitoBean private SearchService searchService;
    @Autowired private ObjectMapper objectMapper;
 
    @Test
    void search_shouldReturnOk() throws Exception {
        JobSearchRecord record = new JobSearchRecord();
        record.setJobId("job-1");
        record.setTitle("Software Engineer");
 
        when(searchService.search(anyString(), anyString()))
            .thenReturn(Collections.singletonList(record));
 
        mockMvc.perform(get("/api/search/jobs")
                .param("keyword", "software")
                .param("location", "remote"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].jobId").value("job-1"));
    }
 
    @Test
    void search_noParams_shouldReturnOk() throws Exception {
        when(searchService.search(null, null)).thenReturn(Collections.emptyList());
 
        mockMvc.perform(get("/api/search/jobs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }
}
