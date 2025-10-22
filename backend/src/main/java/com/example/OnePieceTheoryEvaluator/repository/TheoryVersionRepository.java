package com.example.OnePieceTheoryEvaluator.repository;

import com.example.OnePieceTheoryEvaluator.entity.Theory;
import com.example.OnePieceTheoryEvaluator.entity.TheoryVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TheoryVersionRepository extends JpaRepository<TheoryVersion, Long> {
    List<TheoryVersion> findByTheoryOrderByVersionNumberDesc(Theory theory);
    TheoryVersion findFirstByTheoryOrderByVersionNumberDesc(Theory theory);
}


