package com.example.OnePieceTheoryEvaluator.repository;

import com.example.OnePieceTheoryEvaluator.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    
    /**
     * Find a bookmark by user ID and theory ID
     */
    Optional<Bookmark> findByUserIdAndTheoryId(Long userId, Long theoryId);
    
    /**
     * Find all bookmarks for a specific user
     */
    List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Find all bookmarks for a specific theory
     */
    List<Bookmark> findByTheoryIdOrderByCreatedAtDesc(Long theoryId);
    
    /**
     * Check if a user has bookmarked a theory
     */
    boolean existsByUserIdAndTheoryId(Long userId, Long theoryId);
    
    /**
     * Count bookmarks for a theory
     */
    long countByTheoryId(Long theoryId);
    
    /**
     * Get all theory IDs bookmarked by a user
     */
    @Query("SELECT b.theory.id FROM Bookmark b WHERE b.user.id = :userId")
    List<Long> findTheoryIdsByUserId(@Param("userId") Long userId);
}

