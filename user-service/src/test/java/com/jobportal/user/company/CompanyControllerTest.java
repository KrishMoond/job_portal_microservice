package com.jobportal.user.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompanyControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private CompanyRepository companyRepository;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void getCompany_found_shouldReturnOk() throws Exception {
        Company company = new Company();
        company.setId("comp-1");
        company.setName("Tech Corp");

        when(companyRepository.findById("comp-1")).thenReturn(Optional.of(company));

        mockMvc.perform(get("/api/companies/comp-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tech Corp"));
    }

    @Test
    void createCompany_shouldReturnCreated() throws Exception {
        Company company = new Company();
        company.setName("Tech Corp");
        company.setWebsite("https://techcorp.com");

        when(companyRepository.existsByName("Tech Corp")).thenReturn(false);
        when(companyRepository.save(any())).thenReturn(company);

        mockMvc.perform(post("/api/companies")
                .header("X-User-Id", "admin-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(company)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Tech Corp"));
    }
}
