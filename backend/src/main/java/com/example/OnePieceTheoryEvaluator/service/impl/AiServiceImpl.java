package com.example.OnePieceTheoryEvaluator.service.impl;

import com.example.OnePieceTheoryEvaluator.entity.Theory;
import com.example.OnePieceTheoryEvaluator.exceptions.NotFoundException;
import com.example.OnePieceTheoryEvaluator.repository.TheoryRepository;
import com.example.OnePieceTheoryEvaluator.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class AiServiceImpl implements AiService {

    private final TheoryRepository theoryRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // @Value("${mistral.api.url:https://api.mistral.ai/v1/chat/completions}")
    // private String mistralApiUrl;

    // @Value("${mistral.api.key}")
    // private String mistralApiKey;

    @Value("${ai.proxy.url}")
    private String aiProxyUrl;

    public AiServiceImpl(TheoryRepository theoryRepository) {
        this.theoryRepository = theoryRepository;
    }

    @Override
    public int evaluateTheory(Long theoryId) {
        return evaluateTheoryWithJustification(theoryId).getScore();
    }
    
    @Override
    public AiService.AiEvaluationResult evaluateTheoryWithJustification(Long theoryId) {
        Theory t = theoryRepository.findById(theoryId)
                .orElseThrow(() -> new NotFoundException("Theory Not Found"));

        String title = t.getTitle() == null ? "" : t.getTitle();
        String content = t.getContent() == null ? "" : t.getContent();
        
        // Log original content for debugging
        System.out.println("Original title length: " + title.length());
        System.out.println("Original content length: " + content.length());
        
        // Sanitize and truncate content
        String sanitizedTitle = escape(title);
        String sanitizedContent = escape(content.length() > 3000 ? content.substring(0, 3000) : content);
        
        System.out.println("Sanitized title length: " + sanitizedTitle.length());
        System.out.println("Sanitized content length: " + sanitizedContent.length());
        
        // Create JSON payload with sanitized content using safe JSON creation
        String payload = createSafeJsonPayload(sanitizedTitle, sanitizedContent);
        
        System.out.println("JSON payload length: " + payload.length());
        System.out.println("JSON payload preview: " + payload.substring(0, Math.min(200, payload.length())) + "...");
        
        // Validate JSON before sending
        try {
            new com.fasterxml.jackson.databind.ObjectMapper().readTree(payload);
            System.out.println("JSON validation successful");
        } catch (Exception jsonError) {
            System.out.println("JSON validation failed: " + jsonError.getMessage());
            return new AiService.AiEvaluationResult(-110, "Erreur de format JSON: " + jsonError.getMessage());
        }

        HttpRequest req = HttpRequest.newBuilder()
                // Service Docker → résolu par le nom 'ai-proxy'
                .uri(URI.create(aiProxyUrl + "/score"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("AI-Proxy Response - Status: " + resp.statusCode() + ", Body: " + resp.body());
            
            if (resp.statusCode() >= 300) {
                System.out.println("AI-Proxy error: status=" + resp.statusCode() + " body=" + resp.body());
                return new AiService.AiEvaluationResult(-110, "Erreur de communication avec l'IA");
            }
            com.fasterxml.jackson.databind.JsonNode node =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(resp.body());
            
            System.out.println("Parsed JSON node: " + node.toString());
            System.out.println("aiScore field exists: " + node.has("aiScore"));
            System.out.println("aiScore value as text: " + node.path("aiScore").asText());
            System.out.println("aiScore value as int: " + node.path("aiScore").asInt());
            System.out.println("aiScore is null: " + node.path("aiScore").isNull());
            System.out.println("aiScore is missing: " + node.path("aiScore").isMissingNode());
            
            // Handle 0 value properly
            int score;
            if (node.path("aiScore").isNull() || node.path("aiScore").isMissingNode()) {
                score = -110; // Default only if missing
                System.out.println("Using default score -110 because aiScore is missing/null");
            } else {
                score = node.path("aiScore").asInt();
                System.out.println("Using parsed score: " + score);
            }
            
            String justification = node.path("justification").asText("Aucune justification disponible");
            
            System.out.println("Final parsed score: " + score);
            int finalScore = (score == -200) ? -200 : Math.max(-110, Math.min(100, score));
            return new AiService.AiEvaluationResult(
                finalScore, 
                justification
            );
        } catch (Exception e) {
            System.out.println("Exception in AI service: " + e.getMessage());
            e.printStackTrace();
            return new AiService.AiEvaluationResult(-110, "Erreur lors de l'évaluation: " + e.getMessage());
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        
        try {
            // Comprehensive sanitization for JSON safety
            return s
                // Basic JSON escaping
                .replace("\\", "\\\\")  // Escape backslashes first
                .replace("\"", "\\\"")  // Escape quotes
                .replace("\b", "\\b")   // Escape backspace
                .replace("\f", "\\f")    // Escape form feed
                .replace("\n", "\\n")    // Escape newlines
                .replace("\r", "\\r")    // Escape carriage returns
                .replace("\t", "\\t")    // Escape tabs
                // Remove or replace other control characters
                .replace("\0", "")      // Remove null characters
                .replaceAll("[\\x00-\\x1F\\x7F]", "") // Remove other control characters
                // Additional safety measures
                .replaceAll("[\u0000-\u001F\u007F-\u009F]", "") // Remove Unicode control characters
                .trim(); // Remove leading/trailing whitespace
        } catch (Exception e) {
            System.out.println("Error in escape method, using fallback: " + e.getMessage());
            // Fallback: aggressive character removal
            return s.replaceAll("[^\\x20-\\x7E]", "").trim();
        }
    }
    
    private static String createSafeJsonPayload(String title, String content) {
        try {
            // Use Jackson ObjectMapper for safe JSON creation
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, String> data = new java.util.HashMap<>();
            data.put("title", title);
            data.put("content", content);
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            System.out.println("Error creating JSON with ObjectMapper, using manual creation: " + e.getMessage());
            // Fallback to manual JSON creation
            return "{\"title\":\"" + escape(title) + "\",\"content\":\"" + escape(content) + "\"}";
        }
    }

    private static int parsePercent(String s) {
        if (s == null) return 50;
        s = s.trim();
        StringBuilder digits = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) digits.append(c);
        }
        try {
            return Integer.parseInt(digits.toString());
        } catch (Exception e) {
            return 50;
        }
    }
}