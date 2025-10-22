package com.example.OnePieceTheoryEvaluator.controller;

import com.example.OnePieceTheoryEvaluator.entity.DailyVoteSeries;
import com.example.OnePieceTheoryEvaluator.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  public AnalyticsController(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  @PostMapping("/snapshots/refresh")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> refresh(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
    analyticsService.refreshDailySeries(day);
    return ResponseEntity.ok(Map.of("status", "ok", "day", day));
  }

  @GetMapping("/theories/{id}/daily-series")
  public List<DailyVoteSeries> series(
      @PathVariable Long id,
      @RequestParam(defaultValue = "30") int days) {
    return analyticsService.getSeries(id, days);
  }

    @PostMapping("/fake-backfill")
    public ResponseEntity<?> fakeBackfill(@RequestParam(defaultValue = "30") int days){
    analyticsService.fakeBackfill(days);
    return ResponseEntity.ok(Map.of("status","ok","days",days));
    }
}
