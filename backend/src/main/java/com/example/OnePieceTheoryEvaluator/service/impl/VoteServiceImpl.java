package com.example.OnePieceTheoryEvaluator.service.impl;

import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.dto.VoteDTO;
import com.example.OnePieceTheoryEvaluator.entity.Theory;
import com.example.OnePieceTheoryEvaluator.entity.User;
import com.example.OnePieceTheoryEvaluator.entity.Vote;
import com.example.OnePieceTheoryEvaluator.enums.VoteType;
import com.example.OnePieceTheoryEvaluator.exceptions.NotFoundException;
import com.example.OnePieceTheoryEvaluator.repository.TheoryRepository;
import com.example.OnePieceTheoryEvaluator.repository.VoteRepository;
import com.example.OnePieceTheoryEvaluator.service.UserService;
import com.example.OnePieceTheoryEvaluator.service.VoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final TheoryRepository theoryRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;

    @Override
    @Transactional
    public Response voteUp(VoteDTO voteDTO) {
        return handleVote(voteDTO, VoteType.UP);
    }

    @Override
    @Transactional
    public Response voteDown(VoteDTO voteDTO) {
        return handleVote(voteDTO, VoteType.DOWN);
    }

    @Override
    @Transactional
    public Response unvote(VoteDTO voteDTO) {
        if (voteDTO.getTheoryId() == null) {
            throw new NotFoundException("Theory ID is required");
        }
        User currentUser = userService.getCurrentLoggedInUser();

        voteRepository.findByTheoryIdAndUserId(voteDTO.getTheoryId(), currentUser.getId())
                .ifPresent(voteRepository::delete);

        return Response.builder()
                .status(200)
                .message("Unvoted successfully")
                .build();
    }

    @Override
    public Response getAllTheoryUpVotes(Long theoryId) {
        List<Vote> votes = voteRepository.findByTheoryIdAndTypeOrderByIdDesc(theoryId, VoteType.UP);
        List<VoteDTO> voteDTOs = votes.stream().map(v -> {
            VoteDTO dto = modelMapper.map(v, VoteDTO.class);
            dto.setTheoryId(null);
            if (dto.getUser() != null) {
                dto.getUser().setComments(null);
                dto.getUser().setTheories(null);
                dto.getUser().setEmail(null);
                dto.getUser().setRole(null);
                dto.getUser().setCreatedAt(null);
            }
            return dto;
        }).toList();
        return Response.builder()
                .status(200)
                .message("success")
                .votes(voteDTOs)
                .build();
    }

    @Override
    public Response getAllTheoryDownVotes(Long theoryId) {
        List<Vote> votes = voteRepository.findByTheoryIdAndTypeOrderByIdDesc(theoryId, VoteType.DOWN);
        List<VoteDTO> voteDTOs = votes.stream().map(v -> {
            VoteDTO dto = modelMapper.map(v, VoteDTO.class);
            dto.setTheoryId(null);
            if (dto.getUser() != null) {
                dto.getUser().setComments(null);
                dto.getUser().setTheories(null);
                dto.getUser().setEmail(null);
                dto.getUser().setRole(null);
                dto.getUser().setCreatedAt(null);
            }
            return dto;
        }).toList();
        return Response.builder()
                .status(200)
                .message("success")
                .votes(voteDTOs)
                .build();
    }

    @Override
    public Response getAllUserVotes(Long userId) {
        List<Vote> votes = voteRepository.findByUserIdOrderByIdDesc(userId);
        List<VoteDTO> voteDTOs = votes.stream().map(v -> {
            VoteDTO dto = modelMapper.map(v, VoteDTO.class);
            dto.setTheoryId(v.getTheory() != null ? v.getTheory().getId() : null);
            dto.setUser(null);
            return dto;
        }).toList();
        return Response.builder()
                .status(200)
                .message("success")
                .votes(voteDTOs)
                .build();
    }

    @Override
    public Response getUserVote(Long userId, Long theoryId) {
        // return empty vote if not found
        Vote vote = voteRepository.findByUserIdAndTheoryId(userId, theoryId)
                .orElse(null);
        if (vote == null) {
            return Response.builder()
                    .status(200)
                    .message("No votes found")
                    .vote(null)
                    .build();
        }

        // Keep only id type and created at
        vote.setUser(null);
        vote.setTheory(null);
        VoteDTO voteDTO = modelMapper.map(vote, VoteDTO.class);
        voteDTO.setUser(null);
        voteDTO.setTheoryId(null);
        return Response.builder()
                .status(200)
                .message("success")
                .vote(voteDTO)
                .build();
    }

    private Response handleVote(VoteDTO voteDTO, VoteType targetType) {
        if (voteDTO.getTheoryId() == null) {
            throw new NotFoundException("Theory ID is required");
        }

        User currentUser = userService.getCurrentLoggedInUser();
        Theory theory = theoryRepository.findById(voteDTO.getTheoryId())
                .orElseThrow(() -> new NotFoundException("Theory Not Found"));

        voteRepository.findByTheoryIdAndUserId(theory.getId(), currentUser.getId())
                .ifPresentOrElse(existing -> {
                    if (existing.getType() == targetType) {
                        // toggle off
                        voteRepository.delete(existing);
                    } else {
                        existing.setType(targetType);
                        voteRepository.save(existing);
                    }
                }, () -> {
                    Vote vote = Vote.builder()
                            .type(targetType)
                            .user(currentUser)
                            .theory(theory)
                            .build();
                    voteRepository.save(vote);
                });

        return Response.builder()
                .status(200)
                .message(targetType == VoteType.UP ? "Upvoted successfully" : "Downvoted successfully")
                .build();
    }
}
