package com.jobportal.user.bookmark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookmarkController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookmarkControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private BookmarkRepository bookmarkRepository;

    @Test
    void getMyBookmarks_shouldReturnOk() throws Exception {
        when(bookmarkRepository.findByUserId("user-1")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/bookmarks")
                .header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void addBookmark_shouldReturnCreated() throws Exception {
        when(bookmarkRepository.existsByUserIdAndJobId("user-1", "job-1")).thenReturn(false);
        Bookmark bookmark = new Bookmark();
        bookmark.setUserId("user-1");
        bookmark.setJobId("job-1");
        when(bookmarkRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(bookmark);

        mockMvc.perform(post("/api/bookmarks/job-1")
                .header("X-User-Id", "user-1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.jobId").value("job-1"));
    }
}
