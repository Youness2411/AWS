package com.example.OnePieceTheoryEvaluator.service;

import com.example.OnePieceTheoryEvaluator.dto.CommentDTO;
import com.example.OnePieceTheoryEvaluator.dto.Response;

public interface CommentService {
    Response postComment(CommentDTO commentDTO);
    Response updateComment(Long id, CommentDTO commentDTO);
    Response deleteComment(Long id);
    Response getAllTheoryComments(Long id); // Theory ID
    Response getAllUserComments(Long id); // User ID
    Response getCommentById(Long id);
}
