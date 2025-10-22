package com.example.OnePieceTheoryEvaluator.service;

import com.example.OnePieceTheoryEvaluator.dto.BookmarkDTO;
import com.example.OnePieceTheoryEvaluator.dto.Response;

public interface BookmarkService {
    
    /**
     * Add a theory to user's bookmarks
     */
    Response addBookmark(BookmarkDTO bookmarkDTO);
    
    /**
     * Remove a theory from user's bookmarks
     */
    Response removeBookmark(Long theoryId);
    
    /**
     * Get all bookmarks for the current user
     */
    Response getUserBookmarks();
    
    /**
     * Check if current user has bookmarked a theory
     */
    Response isBookmarked(Long theoryId);
    
    /**
     * Get all users who bookmarked a specific theory
     */
    Response getTheoryBookmarks(Long theoryId);
    
    /**
     * Get bookmark count for a theory
     */
    Response getBookmarkCount(Long theoryId);
}

