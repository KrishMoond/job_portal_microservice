package com.jobportal.search.service;
 
import com.jobportal.search.model.JobSearchRecord;
import com.jobportal.search.repository.JobSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.util.Collections;
import java.util.List;
 
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
 
@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
 
    @Mock private JobSearchRepository repository;
    @InjectMocks private SearchService searchService;
 
    private JobSearchRecord record;
 
    @BeforeEach
    void setUp() {
        record = new JobSearchRecord();
        record.setJobId("job-1");
        record.setTitle("Java Dev");
        record.setLocation("Berlin");
    }
 
    @Test
    void search_withKeywordAndLocation_shouldCallRepo() {
        when(repository.findByKeywordAndLocation("java", "berlin"))
            .thenReturn(Collections.singletonList(record));
 
        List<JobSearchRecord> results = searchService.search("java", "berlin");
 
        assertThat(results).hasSize(1);
        verify(repository).findByKeywordAndLocation("java", "berlin");
    }
 
    @Test
    void search_withOnlyKeyword_shouldCallRepo() {
        when(repository.findByKeyword("java")).thenReturn(Collections.singletonList(record));
 
        List<JobSearchRecord> results = searchService.search("java", "");
 
        assertThat(results).hasSize(1);
        verify(repository).findByKeyword("java");
    }
 
    @Test
    void search_withOnlyLocation_shouldCallRepo() {
        when(repository.findByLocationContaining("berlin")).thenReturn(Collections.singletonList(record));
 
        List<JobSearchRecord> results = searchService.search(null, "berlin");
 
        assertThat(results).hasSize(1);
        verify(repository).findByLocationContaining("berlin");
    }
 
    @Test
    void search_withNoFilters_shouldReturnOpenJobs() {
        when(repository.findByStatus("OPEN")).thenReturn(Collections.singletonList(record));
 
        List<JobSearchRecord> results = searchService.search(null, null);
 
        assertThat(results).hasSize(1);
        verify(repository).findByStatus("OPEN");
    }
}
