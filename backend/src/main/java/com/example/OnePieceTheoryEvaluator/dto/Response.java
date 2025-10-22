package com.example.OnePieceTheoryEvaluator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.example.OnePieceTheoryEvaluator.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    //generic
    private int status;
    private String message;
    //for login
    private String token;
    private UserRole role;
    private String expirationTime;

    //for pagination
    private Integer totalPages;
    private Long totalElements;

    //data output optional
    private UserDTO user;
    private List<UserDTO> users;

    private TheoryDTO theory;
    private List<TheoryDTO> theories;

    private CommentDTO comment;
    private List<CommentDTO> comments;

    private VoteDTO vote;
    private List<VoteDTO> votes;

    // bookmarks
    private BookmarkDTO bookmark;
    private List<BookmarkDTO> bookmarks;

    // generic upload helper
    private String url;

    // versions
    private com.example.OnePieceTheoryEvaluator.dto.TheoryVersionDTO version;
    private List<com.example.OnePieceTheoryEvaluator.dto.TheoryVersionDTO> versions;

    private final LocalDateTime timestamp = LocalDateTime.now();

}