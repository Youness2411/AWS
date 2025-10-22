package com.example.OnePieceTheoryEvaluator.service;

import com.example.OnePieceTheoryEvaluator.service.AnalyticsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class AnalyticsScheduler {

    private static final ZoneId ZONE = ZoneId.of("Europe/Paris");
    private final AnalyticsService analyticsService;

    public AnalyticsScheduler(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // Tous les jours à 00h05 heure de Paris
    @Scheduled(cron = "0 5 0 * * *", zone = "Europe/Paris")
    public void refreshDailySeriesJob() {
        LocalDate day = LocalDate.now(ZONE);
        analyticsService.refreshDailySeries(day);
    }
}
