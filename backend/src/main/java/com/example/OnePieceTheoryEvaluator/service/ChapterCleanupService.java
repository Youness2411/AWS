package com.example.OnePieceTheoryEvaluator.service;

import com.example.OnePieceTheoryEvaluator.repository.TheoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChapterCleanupService {

    private final TheoryRepository theoryRepository;

    /**
     * Clear the isRelatedToLastChapter flag for all theories every Friday at 12:00 PM
     * This runs weekly as chapters are released roughly every Friday
     */
    @Scheduled(cron = "0 0 12 * * FRI")
    @Transactional
    public void clearLastChapterFlags() {
        try {
            int updatedCount = theoryRepository.clearLastChapterFlags();
            log.info("Chapter cleanup completed: {} theories no longer marked as related to last chapter", updatedCount);
        } catch (Exception e) {
            log.error("Error during chapter cleanup: {}", e.getMessage(), e);
        }
    }
}
