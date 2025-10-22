package com.example.OnePieceTheoryEvaluator.service.impl;

import com.example.OnePieceTheoryEvaluator.dto.CommentDTO;
import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.dto.UserDTO;
import com.example.OnePieceTheoryEvaluator.entity.Comment;
import com.example.OnePieceTheoryEvaluator.entity.Theory;
import com.example.OnePieceTheoryEvaluator.entity.User;
import com.example.OnePieceTheoryEvaluator.exceptions.NotFoundException;
import com.example.OnePieceTheoryEvaluator.repository.CommentRepository;
import com.example.OnePieceTheoryEvaluator.repository.TheoryRepository;
import com.example.OnePieceTheoryEvaluator.service.CommentService;
import com.example.OnePieceTheoryEvaluator.service.UserService;
import com.example.OnePieceTheoryEvaluator.security.MarkdownSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TheoryRepository theoryRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final MarkdownSecurityService markdownSecurityService;

    @Override
    @Transactional
    public Response postComment(CommentDTO commentDTO) {
        if (commentDTO.getTheoryId() == null) {
            throw new NotFoundException("Theory ID is required");
        }

        Theory theory = theoryRepository.findById(commentDTO.getTheoryId())
                .orElseThrow(() -> new NotFoundException("Theory Not Found"));

               User currentUser = userService.getCurrentLoggedInUser();

               // Sanitize comment content
               String sanitizedContent = markdownSecurityService.sanitizeMarkdown(commentDTO.getContent());

               Comment comment = Comment.builder()
                       .content(sanitizedContent)
                       .user(currentUser)
                       .theory(theory)
                       .build();

        if (commentDTO.getParentId() != null) {
            Comment parent = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent Comment Not Found"));
            // Ensure parent belongs to same theory
            if (parent.getTheory() == null || !parent.getTheory().getId().equals(theory.getId())) {
                throw new NotFoundException("Parent comment does not belong to the specified theory");
            }
            comment.setParent(parent);
        }

        commentRepository.save(comment);

        return Response.builder()
                .status(200)
                .message("Comment posted successfully")
                .build();
    }

    @Override
    @Transactional
    public Response updateComment(Long id, CommentDTO commentDTO) {
        if (id == null) throw new NotFoundException("Comment ID is required");

        Comment existing = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment Not Found"));

        // allow owner or admin only
        User current = userService.getCurrentLoggedInUser();
        boolean isOwner = existing.getUser() != null && existing.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRole() != null && current.getRole().name().equals("ADMIN");
        boolean isModerator = current.getRole() != null && current.getRole().name().equals("MODERATOR");
        if (!isOwner && !isAdmin && !isModerator) throw new NotFoundException("Not authorized to update this theory");

        if (commentDTO.getContent() != null) {
            // Sanitize comment content
            String sanitizedContent = markdownSecurityService.sanitizeMarkdown(commentDTO.getContent());
            existing.setContent(sanitizedContent);
        }
        existing.setUpdatedAt(LocalDateTime.now());

        commentRepository.save(existing);

        return Response.builder()
                .status(200)
                .message("Comment Successfully updated")
                .build();
    }

    @Override
    public Response deleteComment(Long id) {
        Comment existing = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment Not Found"));
        
                // allow owner or admin only
        User current = userService.getCurrentLoggedInUser();
        boolean isOwner = existing.getUser() != null && existing.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRole() != null && current.getRole().name().equals("ADMIN");
        boolean isModerator = current.getRole() != null && current.getRole().name().equals("MODERATOR");
        if (!isOwner && !isAdmin && !isModerator) throw new NotFoundException("Not authorized to update this theory");
        
        commentRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Comment Successfully Deleted")
                .build();
    }

    @Override
    public Response getAllTheoryComments(Long theoryId) {
        List<Comment> allComments = commentRepository.findByTheoryId(theoryId);

        // Build DTO map
        Map<Long, CommentDTO> idToDto = new HashMap<>();
        List<CommentDTO> roots = new ArrayList<>();

        for (Comment c : allComments) {
            CommentDTO dto = toShallowDto(c);
            idToDto.put(c.getId(), dto);
        }

        // Link children
        for (Comment c : allComments) {
            CommentDTO dto = idToDto.get(c.getId());
            if (c.getParent() != null) {
                CommentDTO parentDto = idToDto.get(c.getParent().getId());
                if (parentDto != null) {
                    if (parentDto.getReplies() == null) parentDto.setReplies(new ArrayList<>());
                    parentDto.getReplies().add(dto);
                }
            } else {
                roots.add(dto);
            }
        }

        return Response.builder()
                .status(200)
                .message("success")
                .comments(roots)
                .build();
    }

    @Override
    public Response getAllUserComments(Long userId) {
        List<Comment> comments = commentRepository.findByUserIdOrderByIdDesc(userId);
        List<CommentDTO> commentDTOs = modelMapper.map(comments, new TypeToken<List<CommentDTO>>() {}.getType());

        commentDTOs.forEach(c -> {
            c.setUser(null);
        });

        return Response.builder()
                .status(200)
                .message("success")
                .comments(commentDTOs)
                .build();
    }

    @Override
    public Response getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment Not Found"));

        CommentDTO commentDTO = toShallowDto(comment);

        return Response.builder()
                .status(200)
                .message("success")
                .comment(commentDTO)
                .build();
    }

    private CommentDTO toShallowDto(Comment c) {
        CommentDTO dto = modelMapper.map(c, CommentDTO.class);
        // sanitize
        dto.setTheoryId(null);
        dto.setParentId(c.getParent() != null ? c.getParent().getId() : null);
        dto.setReplies(null);
        UserDTO userDTO = dto.getUser();
        if (userDTO != null) {
            userDTO.setTheories(null);
            userDTO.setComments(null);
            userDTO.setEmail(null);
            userDTO.setRole(null);
            userDTO.setCreatedAt(null);
        }
        return dto;
    }
}
