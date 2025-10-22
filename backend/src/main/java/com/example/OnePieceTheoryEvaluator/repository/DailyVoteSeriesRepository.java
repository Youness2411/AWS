package com.example.OnePieceTheoryEvaluator.repository;

import com.example.OnePieceTheoryEvaluator.entity.DailyVoteSeries;
import com.example.OnePieceTheoryEvaluator.entity.DailyVoteSeriesId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyVoteSeriesRepository extends JpaRepository<DailyVoteSeries, DailyVoteSeriesId> {
  List<DailyVoteSeries> findByIdTheoryIdAndIdDayGreaterThanEqualOrderByIdDay(Long theoryId, LocalDate since);
}
