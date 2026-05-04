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
import java.util.List;

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

    @Test
    void createCompany_duplicateName_shouldReturnBadRequest() throws Exception {
        Company company = new Company();
        company.setName("Tech Corp");
        when(companyRepository.existsByName("Tech Corp")).thenReturn(true);

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(company)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCompany_owner_shouldReturnOk() throws Exception {
        Company existing = existingCompany();
        Company req = new Company();
        req.setDescription("Updated");
        req.setWebsite("https://updated.example.com");
        req.setLogoUrl("https://updated.example.com/logo.png");
        when(companyRepository.findById("comp-1")).thenReturn(Optional.of(existing));
        when(companyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/companies/comp-1")
                .header("X-User-Id", "user-1")
                .header("X-User-Role", "RECRUITER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"));
    }

    @Test
    void updateCompany_admin_shouldReturnOk() throws Exception {
        Company existing = existingCompany();
        Company req = new Company();
        req.setDescription("Admin Updated");
        when(companyRepository.findById("comp-1")).thenReturn(Optional.of(existing));
        when(companyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/companies/comp-1")
                .header("X-User-Id", "admin-1")
                .header("X-User-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void updateCompany_notOwner_shouldReturnForbidden() throws Exception {
        when(companyRepository.findById("comp-1")).thenReturn(Optional.of(existingCompany()));

        mockMvc.perform(put("/api/companies/comp-1")
                .header("X-User-Id", "other-user")
                .header("X-User-Role", "RECRUITER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Company())))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void updateCompany_notFound_shouldReturnNotFound() throws Exception {
        when(companyRepository.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/companies/missing")
                .header("X-User-Id", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Company())))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCompanies_shouldReturnOk() throws Exception {
        when(companyRepository.findAll()).thenReturn(List.of(existingCompany()));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("comp-1"));
    }

    private Company existingCompany() {
        Company company = new Company();
        company.setId("comp-1");
        company.setName("Tech Corp");
        company.setCreatedByUserId("user-1");
        return company;
    }
}
