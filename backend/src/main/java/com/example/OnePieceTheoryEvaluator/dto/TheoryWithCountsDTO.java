package com.example.OnePieceTheoryEvaluator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TheoryWithCountsDTO {
    
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
    private Integer versionNumber;
    private Integer aiScore;
    private Boolean isRelatedToLastChapter;
    
    // User info
    private Long userId;
    private String username;
    private String userImageUrl;
    
    // Counts
    private Integer commentsCount;
    private Integer upVotesCount;
    private Integer downVotesCount;
    
    /**
     * Factory method to create DTO from Object[] result
     * Order must match the SQL query columns
     */
    public static TheoryWithCountsDTO fromObjectArray(Object[] row) {
        TheoryWithCountsDTO dto = new TheoryWithCountsDTO();
        
        dto.setId(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setTitle((String) row[1]);
        dto.setContent((String) row[2]);
        dto.setImageUrl((String) row[3]);
        dto.setUpdatedAt(row[4] != null ? ((java.sql.Timestamp) row[4]).toLocalDateTime() : null);
        dto.setCreatedAt(row[5] != null ? ((java.sql.Timestamp) row[5]).toLocalDateTime() : null);
        dto.setVersionNumber(row[6] != null ? ((Number) row[6]).intValue() : null);
        dto.setAiScore(row[7] != null ? ((Number) row[7]).intValue() : null);
        dto.setIsRelatedToLastChapter(row[8] != null ? (Boolean) row[8] : false);
        
        // User info
        dto.setUserId(row[9] != null ? ((Number) row[9]).longValue() : null);
        dto.setUsername((String) row[10]);
        dto.setUserImageUrl((String) row[11]);
        
        // Counts
        dto.setCommentsCount(row[12] != null ? ((Number) row[12]).intValue() : 0);
        dto.setUpVotesCount(row[13] != null ? ((Number) row[13]).intValue() : 0);
        dto.setDownVotesCount(row[14] != null ? ((Number) row[14]).intValue() : 0);
        
        return dto;
    }
}
