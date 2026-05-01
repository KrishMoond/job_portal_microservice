package com.jobportal.user.company;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CompanyRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private CompanyRepository companyRepository;

    @Test
    void existsByName_shouldReturnTrue() {
        Company c = new Company();
        c.setName("Big Corp");
        c.setCreatedByUserId("admin");
        entityManager.persist(c);
        entityManager.flush();

        assertThat(companyRepository.existsByName("Big Corp")).isTrue();
    }
}
