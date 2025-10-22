package com.example.OnePieceTheoryEvaluator.service;

import com.example.OnePieceTheoryEvaluator.entity.DailyVoteSeries;
import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {
  void refreshDailySeries(LocalDate day);
  List<DailyVoteSeries> getSeries(Long theoryId, int days);
  void fakeBackfill(int days);
}
