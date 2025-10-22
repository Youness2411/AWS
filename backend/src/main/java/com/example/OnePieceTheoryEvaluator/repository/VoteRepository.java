package com.example.OnePieceTheoryEvaluator.repository;

import com.example.OnePieceTheoryEvaluator.entity.Vote;
import com.example.OnePieceTheoryEvaluator.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByTheoryIdAndUserId(Long theoryId, Long userId);

    List<Vote> findByTheoryIdAndTypeOrderByIdDesc(Long theoryId, VoteType type);

    List<Vote> findByUserIdOrderByIdDesc(Long userId);

    List<Vote> findByTheoryId(Long theoryId);

    @Query(value = """
        SELECT theory_id AS theoryId,
               SUM(CASE WHEN type = 'UP'   THEN 1 ELSE 0 END) AS upCount,
               SUM(CASE WHEN type = 'DOWN' THEN 1 ELSE 0 END) AS downCount
        FROM votes
        GROUP BY theory_id
        """, nativeQuery = true)
    List<VoteAggregationRow> aggregateCountsByTheory();
    Optional<Vote> findByUserIdAndTheoryId(Long userId, Long theoryId);
}
