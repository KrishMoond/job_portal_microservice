package com.jobportal.user.bookmark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookmarkRepositoryTest {

    @Autowired private TestEntityManager entityManager;
    @Autowired private BookmarkRepository bookmarkRepository;

    @Test
    void findByUserId_shouldReturnBookmarks() {
        Bookmark b = new Bookmark();
        b.setUserId("user-123");
        b.setJobId("job-456");
        entityManager.persist(b);
        entityManager.flush();

        List<Bookmark> found = bookmarkRepository.findByUserId("user-123");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getJobId()).isEqualTo("job-456");
    }

    @Test
    void existsByUserIdAndJobId_shouldReturnTrue() {
        Bookmark b = new Bookmark();
        b.setUserId("user-1");
        b.setJobId("job-1");
        entityManager.persist(b);
        entityManager.flush();

        assertThat(bookmarkRepository.existsByUserIdAndJobId("user-1", "job-1")).isTrue();
    }
}
