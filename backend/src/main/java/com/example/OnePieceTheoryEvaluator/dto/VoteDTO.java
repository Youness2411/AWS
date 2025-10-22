package com.example.OnePieceTheoryEvaluator.dto;

import com.example.OnePieceTheoryEvaluator.enums.VoteType;
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
public class VoteDTO {

    private Long id;

    private VoteType type;
        
    private final LocalDateTime createdAt = LocalDateTime.now();

    private UserDTO user;

    private Long theoryId;
}
