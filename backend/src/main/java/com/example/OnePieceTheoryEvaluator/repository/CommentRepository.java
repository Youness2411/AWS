package com.example.OnePieceTheoryEvaluator.repository;

import com.example.OnePieceTheoryEvaluator.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTheoryIdOrderByIdDesc(Long theoryId);
    List<Comment> findByUserIdOrderByIdDesc(Long userId);
    List<Comment> findByTheoryId(Long theoryId);
}
