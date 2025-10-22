package com.example.OnePieceTheoryEvaluator.controller;

import com.example.OnePieceTheoryEvaluator.dto.BookmarkDTO;
import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * Add a theory to bookmarks
     */
    @PostMapping("/add")
    public ResponseEntity<Response> addBookmark(@RequestBody BookmarkDTO bookmarkDTO) {
        return ResponseEntity.ok(bookmarkService.addBookmark(bookmarkDTO));
    }

    /**
     * Remove a theory from bookmarks
     */
    @DeleteMapping("/remove/{theoryId}")
    public ResponseEntity<Response> removeBookmark(@PathVariable Long theoryId) {
        return ResponseEntity.ok(bookmarkService.removeBookmark(theoryId));
    }

    /**
     * Get all bookmarks for current user
     */
    @GetMapping("/my-bookmarks")
    public ResponseEntity<Response> getUserBookmarks() {
        return ResponseEntity.ok(bookmarkService.getUserBookmarks());
    }

    /**
     * Check if current user has bookmarked a theory
     */
    @GetMapping("/is-bookmarked/{theoryId}")
    public ResponseEntity<Response> isBookmarked(@PathVariable Long theoryId) {
        return ResponseEntity.ok(bookmarkService.isBookmarked(theoryId));
    }

    /**
     * Get all users who bookmarked a theory
     */
    @GetMapping("/theory/{theoryId}")
    public ResponseEntity<Response> getTheoryBookmarks(@PathVariable Long theoryId) {
        return ResponseEntity.ok(bookmarkService.getTheoryBookmarks(theoryId));
    }

    /**
     * Get bookmark count for a theory
     */
    @GetMapping("/count/{theoryId}")
    public ResponseEntity<Response> getBookmarkCount(@PathVariable Long theoryId) {
        return ResponseEntity.ok(bookmarkService.getBookmarkCount(theoryId));
    }
}

