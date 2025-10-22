package com.example.OnePieceTheoryEvaluator.repository;

import com.example.OnePieceTheoryEvaluator.entity.Theory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TheoryRepository extends JpaRepository<Theory, Long>{

    @Query("SELECT t FROM Theory t " +
            "WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    List<Theory> findAllByMonthAndYear(@Param("month") int month, @Param("year") int year);


    @Query(value = "SELECT * FROM theories t " +
            "WHERE (:searchText IS NULL OR t.title ILIKE CONCAT('%', :searchText, '%') OR t.content ILIKE CONCAT('%', :searchText, '%')) " +
            "AND t.ai_score >= 0 " +
            "ORDER BY t.id DESC LIMIT :limit", nativeQuery = true)
    List<Theory> searchTheoriesNative(@Param("searchText") String searchText, @Param("limit") int limit);

    @Query(value = """
        SELECT 
            t.id,
            t.title,
            t.content,
            t.image_url,
            t.updated_at,
            t.created_at,
            t.version_number,
            t.ai_score,
            t.is_related_to_last_chapter,
            u.id as user_id,
            u.username,
            u.image_url as user_image_url,
            COALESCE(comment_counts.comment_count, 0) as comments_count,
            COALESCE(up_vote_counts.up_count, 0) as up_votes_count,
            COALESCE(down_vote_counts.down_count, 0) as down_votes_count
        FROM theories t
        LEFT JOIN users u ON t.user_id = u.id
        LEFT JOIN (
            SELECT theory_id, COUNT(*) as comment_count
            FROM comments
            GROUP BY theory_id
        ) comment_counts ON t.id = comment_counts.theory_id
        LEFT JOIN (
            SELECT theory_id, COUNT(*) as up_count
            FROM votes
            WHERE type = 'UP'
            GROUP BY theory_id
        ) up_vote_counts ON t.id = up_vote_counts.theory_id
        LEFT JOIN (
            SELECT theory_id, COUNT(*) as down_count
            FROM votes
            WHERE type = 'DOWN'
            GROUP BY theory_id
        ) down_vote_counts ON t.id = down_vote_counts.theory_id
        WHERE (:searchText IS NULL OR t.title ILIKE CONCAT('%', :searchText, '%') OR t.content ILIKE CONCAT('%', :searchText, '%'))
        AND t.ai_score >= 0
        ORDER BY t.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> searchTheoriesWithCountsNative(@Param("searchText") String searchText, @Param("limit") int limit);

    @Query(value = """
        SELECT 
            t.id,
            t.title,
            t.content,
            t.image_url,
            t.updated_at,
            t.created_at,
            t.version_number,
            t.ai_score,
            t.is_related_to_last_chapter,
            u.id as user_id,
            u.username,
            u.image_url as user_image_url,
            COALESCE(comment_counts.comment_count, 0) as comments_count,
            COALESCE(up_vote_counts.up_count, 0) as up_votes_count,
            COALESCE(down_vote_counts.down_count, 0) as down_votes_count
        FROM theories t
        LEFT JOIN users u ON t.user_id = u.id
        LEFT JOIN (
            SELECT theory_id, COUNT(*) as comment_count
            FROM comments
            GROUP BY theory_id
        ) comment_counts ON t.id = comment_counts.theory_id
        LEFT JOIN (
            SELECT theory_id, COUNT(*) as up_count
            FROM votes
            WHERE type = 'UP'
            GROUP BY theory_id
        ) up_vote_counts ON t.id = up_vote_counts.theory_id
        LEFT JOIN (
            SELECT theory_id, COUNT(*) as down_count
            FROM votes
            WHERE type = 'DOWN'
            GROUP BY theory_id
        ) down_vote_counts ON t.id = down_vote_counts.theory_id
        WHERE (:searchText IS NULL OR t.title ILIKE CONCAT('%', :searchText, '%') OR t.content ILIKE CONCAT('%', :searchText, '%'))
        AND t.ai_score >= 0
        ORDER BY t.id DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> searchTheoriesWithCountsPagedNative(@Param("searchText") String searchText, @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
        SELECT COUNT(*)
        FROM theories t
        WHERE (:searchText IS NULL OR t.title ILIKE CONCAT('%', :searchText, '%') OR t.content ILIKE CONCAT('%', :searchText, '%'))
        AND t.ai_score >= 0
        """, nativeQuery = true)
    long countTheoriesWithSearch(@Param("searchText") String searchText);

    @Query(value = "SELECT * FROM theories t " +
            "WHERE t.ai_score < 0 " +
            "ORDER BY t.id DESC LIMIT :limit", nativeQuery = true)
    List<Theory> findAllFlaggedTheories(@Param("limit") int limit);

    List<Theory> findByUserIdOrderByIdDesc(Long userId);

    @Query(value = """
        SELECT t.*, 
               COALESCE(recent_activity.total_activity, 0) as activity_score
        FROM theories t
        LEFT JOIN (
            SELECT 
                theory_id,
                (COALESCE(vote_count, 0) + COALESCE(comment_count, 0)) as total_activity
            FROM (
                SELECT 
                    COALESCE(v.theory_id, c.theory_id) as theory_id,
                    COALESCE(v.vote_count, 0) as vote_count,
                    COALESCE(c.comment_count, 0) as comment_count
                FROM (
                    SELECT theory_id, COUNT(*) as vote_count
                    FROM votes 
                    WHERE created_at >= NOW() - INTERVAL '24 hours'
                    GROUP BY theory_id
                ) v
                FULL OUTER JOIN (
                    SELECT theory_id, COUNT(*) as comment_count
                    FROM comments 
                    WHERE created_at >= NOW() - INTERVAL '24 hours'
                    GROUP BY theory_id
                ) c ON v.theory_id = c.theory_id
            ) combined
        ) recent_activity ON t.id = recent_activity.theory_id
        WHERE recent_activity.total_activity > 0
        AND t.ai_score >= 0
        ORDER BY activity_score DESC, t.created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Theory> findTrendingTheories(@Param("limit") int limit);

    @Query(value = """
        SELECT t.*, 
               COALESCE(recent_activity.total_activity, 0) as activity_score
        FROM theories t
        LEFT JOIN (
            SELECT 
                theory_id,
                (COALESCE(vote_count, 0) + COALESCE(comment_count, 0)) as total_activity
            FROM (
                SELECT 
                    COALESCE(v.theory_id, c.theory_id) as theory_id,
                    COALESCE(v.vote_count, 0) as vote_count,
                    COALESCE(c.comment_count, 0) as comment_count
                FROM (
                    SELECT theory_id, COUNT(*) as vote_count
                    FROM votes 
                    WHERE created_at >= NOW() - INTERVAL '24 hours'
                    GROUP BY theory_id
                ) v
                FULL OUTER JOIN (
                    SELECT theory_id, COUNT(*) as comment_count
                    FROM comments 
                    WHERE created_at >= NOW() - INTERVAL '24 hours'
                    GROUP BY theory_id
                ) c ON v.theory_id = c.theory_id
            ) combined
        ) recent_activity ON t.id = recent_activity.theory_id
        WHERE t.is_related_to_last_chapter = true
        AND t.ai_score >= 0
        ORDER BY activity_score DESC, t.created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Theory> findLastChapterTheories(@Param("limit") int limit);

    @Modifying
    @Query(value = "UPDATE theories SET is_related_to_last_chapter = false WHERE is_related_to_last_chapter = true", nativeQuery = true)
    int clearLastChapterFlags();

    @Query(value = """
        SELECT 
            t.id,
            t.title,
            t.content,
            t.image_url,
            t.updated_at,
            t.created_at,
            t.version_number,
            t.ai_score,
            t.is_related_to_last_chapter,
            u.id as user_id,
            u.username,
            u.image_url as user_image_url,
            COALESCE(comment_counts.comment_count, 0) as comments_count,
            COALESCE(up_vote_counts.up_count, 0) as up_votes_count,
            COALESCE(down_vote_counts.down_count, 0) as down_votes_count
        FROM theories t
        LEFT JOIN users u ON t.user_id = u.id
        LEFT JOIN (
            SELECT theory_id, COUNT(*) as comment_count
            FROM comments
            GROUP BY theory_id
        ) comment_counts ON t.id = comment_counts.theory_id
        LEFT JOIN (
            SELECT theory_id, COUNT(*) as up_count
            FROM votes
            WHERE type = 'UP'
            GROUP BY theory_id
        ) up_vote_counts ON t.id = up_vote_counts.theory_id
        LEFT JOIN (
            SELECT theory_id, COUNT(*) as down_count
            FROM votes
            WHERE type = 'DOWN'
            GROUP BY theory_id
        ) down_vote_counts ON t.id = down_vote_counts.theory_id
        WHERE t.ai_score >= 0
        ORDER BY t.id DESC
        """, nativeQuery = true)
    List<Object[]> findAllWithCounts();

    @Query(value = """
        SELECT 
            t.id,
            t.title,
            t.content,
            t.image_url,
            t.updated_at,
            t.created_at,
            t.version_number,
            t.ai_score,
            t.is_related_to_last_chapter,
            u.id as user_id,
            u.username,
            u.image_url as user_image_url,
            COALESCE(comment_counts.comment_count, 0) as comments_count,
            COALESCE(up_vote_counts.up_count, 0) as up_votes_count,
            COALESCE(down_vote_counts.down_count, 0) as down_votes_count
        FROM theories t
        LEFT JOIN users u ON t.user_id = u.id
        LEFT JOIN (
            SELECT theory_id, COUNT(*) as comment_count
            FROM comments
            GROUP BY theory_id
        ) comment_counts ON t.id = comment_counts.theory_id
        LEFT JOIN (
            SELECT theory_id, COUNT(*) as up_count
            FROM votes
            WHERE type = 'UP'
            GROUP BY theory_id
        ) up_vote_counts ON t.id = up_vote_counts.theory_id
        LEFT JOIN (
            SELECT theory_id, COUNT(*) as down_count
            FROM votes
            WHERE type = 'DOWN'
            GROUP BY theory_id
        ) down_vote_counts ON t.id = down_vote_counts.theory_id
        WHERE t.ai_score >= 0
        ORDER BY t.id DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Object[]> findAllWithCountsPaged(@Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
        SELECT COUNT(*)
        FROM theories t
        """, nativeQuery = true)
    long countAllTheories();

}
