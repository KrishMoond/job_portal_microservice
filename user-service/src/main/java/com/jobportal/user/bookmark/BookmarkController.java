package com.jobportal.user.bookmark;

import com.jobportal.common.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;

    public BookmarkController(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    @PostMapping("/{jobId}")
    @Transactional
    public ResponseEntity<Bookmark> addBookmark(
            @PathVariable String jobId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId) {
        if (bookmarkRepository.existsByUserIdAndJobId(userId, jobId))
            throw new BadRequestException("Job already bookmarked");
        Bookmark bookmark = new Bookmark();
        bookmark.setUserId(userId);
        bookmark.setJobId(jobId);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookmarkRepository.save(bookmark));
    }

    @DeleteMapping("/{jobId}")
    @Transactional
    public ResponseEntity<?> removeBookmark(
            @PathVariable String jobId,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId) {
        bookmarkRepository.deleteByUserIdAndJobId(userId, jobId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Bookmark>> getMyBookmarks(
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "") String userId) {
        return ResponseEntity.ok(bookmarkRepository.findByUserId(userId));
    }
}
