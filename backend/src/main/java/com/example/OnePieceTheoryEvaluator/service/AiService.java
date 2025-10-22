package com.example.OnePieceTheoryEvaluator.service;

public interface AiService {
    /**
     * Retourne un score en pourcentage (0..100) évaluant la crédibilité de la théorie.
     */
    int evaluateTheory(Long theoryId);
    
    /**
     * Retourne un objet contenant le score et la justification de l'évaluation.
     */
    AiEvaluationResult evaluateTheoryWithJustification(Long theoryId);
    
    class AiEvaluationResult {
        private final int score;
        private final String justification;
        
        public AiEvaluationResult(int score, String justification) {
            this.score = score;
            this.justification = justification;
        }
        
        public int getScore() { return score; }
        public String getJustification() { return justification; }
    }
}