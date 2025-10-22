package com.example.OnePieceTheoryEvaluator.controller;

import com.example.OnePieceTheoryEvaluator.dto.CommentDTO;
import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/post")
    public ResponseEntity<Response> postComment(@RequestBody CommentDTO commentDTO){
        return ResponseEntity.ok(commentService.postComment(commentDTO));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updateComment(@PathVariable Long id, @RequestBody CommentDTO commentDTO){
        return ResponseEntity.ok(commentService.updateComment(id, commentDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response> deleteComment(@PathVariable Long id){
        return ResponseEntity.ok(commentService.deleteComment(id));
    }

    @GetMapping("/theory/{theoryId}")
    public ResponseEntity<Response> getAllTheoryComments(@PathVariable Long theoryId){
        return ResponseEntity.ok(commentService.getAllTheoryComments(theoryId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Response> getAllUserComments(@PathVariable Long userId){
        return ResponseEntity.ok(commentService.getAllUserComments(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getCommentById(@PathVariable Long id){
        return ResponseEntity.ok(commentService.getCommentById(id));
    }
}


