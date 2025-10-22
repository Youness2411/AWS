package com.example.OnePieceTheoryEvaluator.controller;

import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.dto.VoteDTO;
import com.example.OnePieceTheoryEvaluator.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/up")
    public ResponseEntity<Response> voteUp(@RequestBody VoteDTO voteDTO){
        return ResponseEntity.ok(voteService.voteUp(voteDTO));
    }

    @PostMapping("/down")
    public ResponseEntity<Response> voteDown(@RequestBody VoteDTO voteDTO){
        return ResponseEntity.ok(voteService.voteDown(voteDTO));
    }

    @PostMapping("/unvote")
    public ResponseEntity<Response> unvote(@RequestBody VoteDTO voteDTO){
        return ResponseEntity.ok(voteService.unvote(voteDTO));
    }

    @GetMapping("/up/{theoryId}")
    public ResponseEntity<Response> getAllTheoryUpVotes(@PathVariable Long theoryId){
        return ResponseEntity.ok(voteService.getAllTheoryUpVotes(theoryId));
    }

    @GetMapping("/down/{theoryId}")
    public ResponseEntity<Response> getAllTheoryDownVotes(@PathVariable Long theoryId){
        return ResponseEntity.ok(voteService.getAllTheoryDownVotes(theoryId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Response> getAllUserVotes(@PathVariable Long userId){
        return ResponseEntity.ok(voteService.getAllUserVotes(userId));
    }

    @GetMapping("/user/{userId}/theory/{theoryId}")
    public ResponseEntity<Response> getUserVote(@PathVariable Long userId, @PathVariable Long theoryId){
        return ResponseEntity.ok(voteService.getUserVote(userId, theoryId));
    }
}


