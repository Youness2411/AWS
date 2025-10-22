package com.example.OnePieceTheoryEvaluator.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.example.OnePieceTheoryEvaluator.enums.UserRole;
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
public class UserDTO {

    private Long id;

    private String username;

    private String email;

    @JsonIgnore
    private String password;

    private String imageUrl;

    private UserRole role;

    private List<TheoryDTO> theories;

    private List<CommentDTO> comments;

    private LocalDateTime createdAt;
}
