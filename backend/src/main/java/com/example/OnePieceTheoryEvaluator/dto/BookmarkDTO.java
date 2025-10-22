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
public class BookmarkDTO {

    private Long id;
    
    private Long theoryId;
    
    private UserDTO user;
    
    private TheoryDTO theory;
    
    private LocalDateTime createdAt;
}

