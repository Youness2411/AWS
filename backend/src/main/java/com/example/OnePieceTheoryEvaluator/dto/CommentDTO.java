package com.example.OnePieceTheoryEvaluator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentDTO {

    private Long id;

    private String content;
    
    private LocalDateTime updatedAt;
    
    private final LocalDateTime createdAt = LocalDateTime.now();

    private UserDTO user;

    private Long theoryId;

    private Long parentId;

    private List<CommentDTO> replies;
}
