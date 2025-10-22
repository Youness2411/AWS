package com.example.OnePieceTheoryEvaluator.service;

import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.dto.VoteDTO;

public interface VoteService {
    Response voteUp(VoteDTO voteDTO);
    Response voteDown(VoteDTO voteDTO);
    Response unvote(VoteDTO voteDTO);
    Response getAllTheoryUpVotes(Long id); // Theory ID
    Response getAllTheoryDownVotes(Long id); // Theory ID
    Response getAllUserVotes(Long id); // User ID
    Response getUserVote(Long userId, Long theoryId); // User ID, Theory ID
}