package com.example.OnePieceTheoryEvaluator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TheoryDTO {

    private Long id;

    private String title;

    private String content;

    private String imageUrl;

    private LocalDateTime updatedAt;
    
    private final LocalDateTime createdAt = LocalDateTime.now();

    private UserDTO user;

    private Integer commentsCount;
    private Integer upVotesCount;
    private Integer downVotesCount;
    private Integer aiScore;
    private Boolean isRelatedToLastChapter;
}
