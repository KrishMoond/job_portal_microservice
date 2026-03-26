package com.jobportal.user.company;

import com.jobportal.common.exception.BadRequestException;
import com.jobportal.common.exception.ForbiddenException;
import com.jobportal.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @PostMapping
    public ResponseEntity<Company> createCompany(
            @Valid @RequestBody Company req,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId) {
        if (companyRepository.existsByName(req.getName()))
            throw new BadRequestException("Company with this name already exists");
        Company company = new Company();
        company.setName(req.getName());
        company.setDescription(req.getDescription());
        company.setWebsite(req.getWebsite());
        company.setLogoUrl(req.getLogoUrl());
        company.setCreatedByUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(companyRepository.save(company));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(
            @PathVariable String id,
            @Valid @RequestBody Company req,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "") String role) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
        if (!"ADMIN".equals(role) && !company.getCreatedByUserId().equals(userId))
            throw new ForbiddenException("You can only edit companies you created");
        company.setDescription(req.getDescription());
        company.setWebsite(req.getWebsite());
        company.setLogoUrl(req.getLogoUrl());
        return ResponseEntity.ok(companyRepository.save(company));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompany(@PathVariable String id) {
        return ResponseEntity.ok(companyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id)));
    }

    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyRepository.findAll());
    }
}
