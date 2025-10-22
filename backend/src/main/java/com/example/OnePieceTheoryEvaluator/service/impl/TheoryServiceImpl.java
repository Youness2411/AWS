package com.example.OnePieceTheoryEvaluator.service.impl;

import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.dto.TheoryDTO;
import com.example.OnePieceTheoryEvaluator.dto.UserDTO;
import com.example.OnePieceTheoryEvaluator.entity.Comment;
import com.example.OnePieceTheoryEvaluator.entity.Theory;
import com.example.OnePieceTheoryEvaluator.entity.User;
import com.example.OnePieceTheoryEvaluator.entity.Vote;
import com.example.OnePieceTheoryEvaluator.entity.TheoryVersion;
import com.example.OnePieceTheoryEvaluator.exceptions.NotFoundException;
import com.example.OnePieceTheoryEvaluator.repository.CommentRepository;
import com.example.OnePieceTheoryEvaluator.repository.TheoryRepository;
import com.example.OnePieceTheoryEvaluator.repository.TheoryVersionRepository;
import com.example.OnePieceTheoryEvaluator.repository.VoteRepository;
import com.example.OnePieceTheoryEvaluator.service.CommentService;
import com.example.OnePieceTheoryEvaluator.service.VoteService;
import com.example.OnePieceTheoryEvaluator.service.TheoryService;
import com.example.OnePieceTheoryEvaluator.service.UserService;
import com.example.OnePieceTheoryEvaluator.service.AiService;
import com.example.OnePieceTheoryEvaluator.util.DatabaseErrorHandler;
import com.example.OnePieceTheoryEvaluator.security.FileUploadSecurityService;
import com.example.OnePieceTheoryEvaluator.security.MarkdownSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TheoryServiceImpl implements TheoryService {

    private final TheoryRepository theoryRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final CommentService commentService;
    private final VoteService voteService;
    private final TheoryVersionRepository theoryVersionRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final AiService aiService;
    private final FileUploadSecurityService fileUploadSecurityService;
    private final MarkdownSecurityService markdownSecurityService;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    
    @Value("${app.images.dir}")
    private String imageDirectory;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public Response postTheory(TheoryDTO theoryDTO, MultipartFile imageFile) {
        User currentUser = userService.getCurrentLoggedInUser();

        // Sanitize markdown content
        String sanitizedContent = markdownSecurityService.sanitizeMarkdown(theoryDTO.getContent());
        
        Theory theory = Theory.builder()
                .title(theoryDTO.getTitle())
                .content(sanitizedContent)
                .user(currentUser)
                .versionNumber(1)
                .isRelatedToLastChapter(theoryDTO.getIsRelatedToLastChapter() != null ? theoryDTO.getIsRelatedToLastChapter() : false)
                .build();

        if (imageFile != null && !imageFile.isEmpty()){
            // Validate file security
            fileUploadSecurityService.validateFile(imageFile);
            String filename = saveImage(imageFile);
            theory.setImageUrl(baseUrl + "/api/uploads/image/" + filename);
        }

        // Save theory with error handling
        DatabaseErrorHandler.executeWithErrorHandling(() -> {
            theoryRepository.save(theory);
            return null;
        });
        
        // Get AI score for the theory
        try {
            int aiScore = aiService.evaluateTheory(theory.getId());
            theory.setAiScore(aiScore);
            DatabaseErrorHandler.executeWithErrorHandling(() -> {
                theoryRepository.save(theory);
                return null;
            });
            log.info("AI score calculated for theory {}: {}", theory.getId(), aiScore);
        } catch (Exception e) {
            log.error("Failed to calculate AI score for theory {}: {}", theory.getId(), e.getMessage());
            // Set default score if AI fails
            theory.setAiScore(-110);
            DatabaseErrorHandler.executeWithErrorHandling(() -> {
                theoryRepository.save(theory);
                return null;
            });
        }
        
        // create initial version
        TheoryVersion v1 = TheoryVersion.builder()
                .theory(theory)
                .versionNumber(1)
                .content(theory.getContent())
                .build();
        DatabaseErrorHandler.executeWithErrorHandling(() -> {
            theoryVersionRepository.save(v1);
            return null;
        });

        String message = "Theory posted successfully";
        if(theory.getAiScore() < 0){
            message = "Theory is under review by the moderation team";
        }

        return Response.builder()
                .status(200)
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public Response updateTheory(Long id, TheoryDTO theoryDTO, MultipartFile imageFile) {
        if (id == null) throw new NotFoundException("Theory ID is required");

        Theory existing = theoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Theory Not Found"));

        // allow owner or admin only
        User current = userService.getCurrentLoggedInUser();
        boolean isOwner = existing.getUser() != null && existing.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRole() != null && current.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) throw new NotFoundException("Not authorized to update this theory");

        //check if image is associated with the update request
        if (imageFile != null && !imageFile.isEmpty()){
            // Validate file security
            fileUploadSecurityService.validateFile(imageFile);
            String filename = saveImage(imageFile);
            existing.setImageUrl(baseUrl + "/api/uploads/image/" + filename);
        }

        if (theoryDTO.getTitle() != null) existing.setTitle(theoryDTO.getTitle());
        if (theoryDTO.getContent() != null) {
            // Sanitize markdown content
            String sanitizedContent = markdownSecurityService.sanitizeMarkdown(theoryDTO.getContent());
            // bump version and create snapshot
            int next = (existing.getVersionNumber() == null ? 1 : existing.getVersionNumber()) + 1;
            existing.setVersionNumber(next);
            String prevContent = existing.getContent(); // Save the previous content
            existing.setContent(sanitizedContent); // update the original content
            TheoryVersion ver = TheoryVersion.builder()
                    .theory(existing)
                    .versionNumber(next - 1)
                    .content(prevContent)
                    .build();
            theoryVersionRepository.save(ver);
        }
        if (theoryDTO.getIsRelatedToLastChapter() != null) {
            existing.setIsRelatedToLastChapter(theoryDTO.getIsRelatedToLastChapter());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        theoryRepository.save(existing);

        return Response.builder()
                .status(200)
                .message("Theory Successfully updated")
                .build();
    }

    @Override
    public Response deleteTheory(Long id) {
        Theory theory = theoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Theory Not Found"));
        
        // Delete associated versions, votes and comments
        TheoryVersion theoryVersion = theoryVersionRepository.findFirstByTheoryOrderByVersionNumberDesc(theory);
        List<Comment> comments = commentRepository.findByTheoryId(id);
        List<Vote> votes = voteRepository.findByTheoryId(id);
        
        
        // allow owner or admin only
        User current = userService.getCurrentLoggedInUser();
        boolean isOwner = theory.getUser() != null && theory.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRole() != null && current.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) throw new NotFoundException("Not authorized to delete this theory");
        
        commentRepository.deleteAll(comments);
        voteRepository.deleteAll(votes);
        theoryVersionRepository.delete(theoryVersion);
        theoryRepository.delete(theory);

        return Response.builder()
                .status(200)
                .message("Theory Successfully Deleted")
                .build();
    }

    @Override
    public Response getAllTheories() {
        // Use optimized query to get all data in one go
        List<Object[]> results = theoryRepository.findAllWithCounts();
        List<TheoryDTO> theoryDTOs = new ArrayList<>();

        // Only allow admin to see flagged theories
        User current = userService.getCurrentLoggedInUser();
        boolean isAdmin = current.getRole() != null && current.getRole().name().equals("ADMIN");
        if (!isAdmin) throw new NotFoundException("Not authorized to view flagged theories");

        for (Object[] row : results) {
            com.example.OnePieceTheoryEvaluator.dto.TheoryWithCountsDTO theoryWithCounts = 
                com.example.OnePieceTheoryEvaluator.dto.TheoryWithCountsDTO.fromObjectArray(row);
            
            // Convert to TheoryDTO
            TheoryDTO theoryDTO = new TheoryDTO();
            theoryDTO.setId(theoryWithCounts.getId());
            theoryDTO.setTitle(theoryWithCounts.getTitle());
            theoryDTO.setContent(theoryWithCounts.getContent());
            theoryDTO.setImageUrl(theoryWithCounts.getImageUrl());
            theoryDTO.setUpdatedAt(theoryWithCounts.getUpdatedAt());
            // createdAt is final in TheoryDTO, so we can't set it
            // versionNumber doesn't exist in TheoryDTO, so we skip it
            theoryDTO.setAiScore(theoryWithCounts.getAiScore() != null ? theoryWithCounts.getAiScore() : 50);
            theoryDTO.setIsRelatedToLastChapter(theoryWithCounts.getIsRelatedToLastChapter());
            
            // Set counts directly from the query
            theoryDTO.setCommentsCount(theoryWithCounts.getCommentsCount());
            theoryDTO.setUpVotesCount(theoryWithCounts.getUpVotesCount());
            theoryDTO.setDownVotesCount(theoryWithCounts.getDownVotesCount());
            
            // Create user DTO
            if (theoryWithCounts.getUserId() != null) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(theoryWithCounts.getUserId());
                userDTO.setUsername(theoryWithCounts.getUsername());
                userDTO.setImageUrl(theoryWithCounts.getUserImageUrl());
                // Don't expose sensitive user info
                userDTO.setEmail(null);
                userDTO.setRole(null);
                userDTO.setCreatedAt(null);
                userDTO.setTheories(null);
                userDTO.setComments(null);
                theoryDTO.setUser(userDTO);
            }
            
            theoryDTOs.add(theoryDTO);
        }

        return Response.builder()
                .status(200)
                .message("success")
                .theories(theoryDTOs)
                .build();
    }

    @Override
    public Response getAllTheories(Pageable pageable) {
        // Use optimized query to get all data in one go
        List<Object[]> results = theoryRepository.findAllWithCounts();
        List<TheoryDTO> theoryDTOs = new ArrayList<>();
        
        // Only allow admin to see flagged theories
        User current = userService.getCurrentLoggedInUser();
        boolean isAdmin = current.getRole() != null && current.getRole().name().equals("ADMIN");
        if (!isAdmin) throw new NotFoundException("Not authorized to view flagged theories");
                
        for (Object[] row : results) {
            com.example.OnePieceTheoryEvaluator.dto.TheoryWithCountsDTO theoryWithCounts = 
                com.example.OnePieceTheoryEvaluator.dto.TheoryWithCountsDTO.fromObjectArray(row);
            
            // Convert to TheoryDTO
            TheoryDTO theoryDTO = new TheoryDTO();
            theoryDTO.setId(theoryWithCounts.getId());
            theoryDTO.setTitle(theoryWithCounts.getTitle());
            theoryDTO.setContent(theoryWithCounts.getContent());
            theoryDTO.setImageUrl(theoryWithCounts.getImageUrl());
            theoryDTO.setUpdatedAt(theoryWithCounts.getUpdatedAt());
            // createdAt is final in TheoryDTO, so we can't set it
            // versionNumber doesn't exist in TheoryDTO, so we skip it
            theoryDTO.setAiScore(theoryWithCounts.getAiScore() != null ? theoryWithCounts.getAiScore() : 50);
            theoryDTO.setIsRelatedToLastChapter(theoryWithCounts.getIsRelatedToLastChapter());
            
            // Set counts directly from the query
            theoryDTO.setCommentsCount(theoryWithCounts.getCommentsCount());
            theoryDTO.setUpVotesCount(theoryWithCounts.getUpVotesCount());
            theoryDTO.setDownVotesCount(theoryWithCounts.getDownVotesCount());
            
            // Create user DTO
            if (theoryWithCounts.getUserId() != null) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(theoryWithCounts.getUserId());
                userDTO.setUsername(theoryWithCounts.getUsername());
                userDTO.setImageUrl(theoryWithCounts.getUserImageUrl());
                // Don't expose sensitive user info
                userDTO.setEmail(null);
                userDTO.setRole(null);
                userDTO.setCreatedAt(null);
                userDTO.setTheories(null);
                userDTO.setComments(null);
                theoryDTO.setUser(userDTO);
            }
            
            theoryDTOs.add(theoryDTO);
        }

        return Response.builder()
                .status(200)
                .message("success")
                .theories(theoryDTOs)
                .build();
    }
    @Override
    public Response getAllTheoriesFlagged() {
        List<Theory> theories = theoryRepository.findAllFlaggedTheories(200);
        List<TheoryDTO> theoryDTOs = modelMapper.map(theories, new TypeToken<List<TheoryDTO>>() {}.getType());

        // Only allow admin to see flagged theories
        User current = userService.getCurrentLoggedInUser();
        boolean isAdmin = current.getRole() != null && current.getRole().name().equals("ADMIN");
        if (!isAdmin) throw new NotFoundException("Not authorized to view flagged theories");

        theoryDTOs.forEach(t -> {
            UserDTO userDTO = t.getUser();
            if (userDTO != null) {
                userDTO.setEmail(null);
                userDTO.setRole(null);
                userDTO.setCreatedAt(null);
                userDTO.setTheories(null);
                userDTO.setComments(null);
            }
        });

        // Fetch comments count and votes count for each theory
        for (TheoryDTO t : theoryDTOs) {
            Response commentsResponse = commentService.getAllTheoryComments(t.getId());
            Response votesResponse = voteService.getAllTheoryUpVotes(t.getId());
            Response downVotesResponse = voteService.getAllTheoryDownVotes(t.getId());
            t.setCommentsCount(countCommentsRecursive(commentsResponse.getComments()));
            t.setUpVotesCount(votesResponse.getVotes().size());
            t.setDownVotesCount(downVotesResponse.getVotes().size());
            
            // Force aiScore mapping if not set
            if (t.getAiScore() == null) {
                Theory theory = theoryRepository.findById(t.getId()).orElse(null);
                t.setAiScore(theory != null && theory.getAiScore() != null ? theory.getAiScore() : 50);
            }
        }

        return Response.builder()
                .status(200)
                .message("success")
                .theories(theoryDTOs)
                .build();
    }

    @Override
    public Response queryTheories(String searchText, String sortBy) {
        // Determine sort: date (default), mostLiked, mostComments
        String normalized = (sortBy == null ? "date" : sortBy).toLowerCase();

        // Use optimized query that gets all data in one go - NO MORE N+1 QUERIES!
        List<Object[]> results = theoryRepository.searchTheoriesWithCountsNative(
                (searchText == null || searchText.isBlank()) ? null : searchText,
                200
        );

        List<TheoryDTO> theoryDTOs = new ArrayList<>();
        
        for (Object[] row : results) {
            com.example.OnePieceTheoryEvaluator.dto.TheoryWithCountsDTO theoryWithCounts = 
                com.example.OnePieceTheoryEvaluator.dto.TheoryWithCountsDTO.fromObjectArray(row);
            
            // Convert to TheoryDTO
            TheoryDTO theoryDTO = new TheoryDTO();
            theoryDTO.setId(theoryWithCounts.getId());
            theoryDTO.setTitle(theoryWithCounts.getTitle());
            theoryDTO.setContent(theoryWithCounts.getContent());
            theoryDTO.setImageUrl(theoryWithCounts.getImageUrl());
            theoryDTO.setUpdatedAt(theoryWithCounts.getUpdatedAt());
            theoryDTO.setAiScore(theoryWithCounts.getAiScore() != null ? theoryWithCounts.getAiScore() : 50);
            theoryDTO.setIsRelatedToLastChapter(theoryWithCounts.getIsRelatedToLastChapter());
            
            // Set counts directly from the query - NO MORE API CALLS!
            theoryDTO.setCommentsCount(theoryWithCounts.getCommentsCount());
            theoryDTO.setUpVotesCount(theoryWithCounts.getUpVotesCount());
            theoryDTO.setDownVotesCount(theoryWithCounts.getDownVotesCount());
            
            // Create user DTO
            if (theoryWithCounts.getUserId() != null) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(theoryWithCounts.getUserId());
                userDTO.setUsername(theoryWithCounts.getUsername());
                userDTO.setImageUrl(theoryWithCounts.getUserImageUrl());
                // Don't expose sensitive user info
                userDTO.setEmail(null);
                userDTO.setRole(null);
                userDTO.setCreatedAt(null);
                userDTO.setTheories(null);
                userDTO.setComments(null);
                theoryDTO.setUser(userDTO);
            }
            
            theoryDTOs.add(theoryDTO);
        }

        // Apply sorting in-memory based on computed counts
        switch (normalized) {
            case "mostliked":
                theoryDTOs.sort((a, b) -> Integer.compare(computeVotesPercentage(b.getUpVotesCount(), b.getDownVotesCount()), computeVotesPercentage(a.getUpVotesCount(), a.getDownVotesCount())));
                break;
            case "mostvotes":
                theoryDTOs.sort((a, b) -> Integer.compare((b.getUpVotesCount() + b.getDownVotesCount()), (a.getUpVotesCount() + a.getDownVotesCount())));
                break;
            case "mostcomments":
                theoryDTOs.sort((a, b) -> Integer.compare(b.getCommentsCount(), a.getCommentsCount()));
                break;
            case "date":
            default:
                // already roughly by id desc from repository search (as proxy for date)
                break;
        }

        return Response.builder()
                .status(200)
                .message("success")
                .theories(theoryDTOs)
                .build();
    }
    @Override
    public Response queryTheories(String searchText, String sortBy, Pageable pageable) {
        // Use optimized paginated query - MUCH FASTER!
        String normalized = (sortBy == null ? "date" : sortBy).toLowerCase();
        
        // Get paginated results directly from database
        List<Object[]> results = theoryRepository.searchTheoriesWithCountsPagedNative(
                (searchText == null || searchText.isBlank()) ? null : searchText,
                pageable.getPageSize(),
                (int) pageable.getOffset()
        );
        
        // Get total count for pagination info
        long totalElements = theoryRepository.countTheoriesWithSearch(
                (searchText == null || searchText.isBlank()) ? null : searchText
        );
        
        List<TheoryDTO> theoryDTOs = new ArrayList<>();
        
        for (Object[] row : results) {
            com.example.OnePieceTheoryEvaluator.dto.TheoryWithCountsDTO theoryWithCounts = 
                com.example.OnePieceTheoryEvaluator.dto.TheoryWithCountsDTO.fromObjectArray(row);
            
            // Convert to TheoryDTO
            TheoryDTO theoryDTO = new TheoryDTO();
            theoryDTO.setId(theoryWithCounts.getId());
            theoryDTO.setTitle(theoryWithCounts.getTitle());
            theoryDTO.setContent(theoryWithCounts.getContent());
            theoryDTO.setImageUrl(theoryWithCounts.getImageUrl());
            theoryDTO.setUpdatedAt(theoryWithCounts.getUpdatedAt());
            theoryDTO.setAiScore(theoryWithCounts.getAiScore() != null ? theoryWithCounts.getAiScore() : 50);
            theoryDTO.setIsRelatedToLastChapter(theoryWithCounts.getIsRelatedToLastChapter());
            
            // Set counts directly from the query
            theoryDTO.setCommentsCount(theoryWithCounts.getCommentsCount());
            theoryDTO.setUpVotesCount(theoryWithCounts.getUpVotesCount());
            theoryDTO.setDownVotesCount(theoryWithCounts.getDownVotesCount());
            
            // Create user DTO
            if (theoryWithCounts.getUserId() != null) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(theoryWithCounts.getUserId());
                userDTO.setUsername(theoryWithCounts.getUsername());
                userDTO.setImageUrl(theoryWithCounts.getUserImageUrl());
                userDTO.setEmail(null);
                userDTO.setRole(null);
                userDTO.setCreatedAt(null);
                userDTO.setTheories(null);
                userDTO.setComments(null);
                theoryDTO.setUser(userDTO);
            }
            
            theoryDTOs.add(theoryDTO);
        }

        // Apply sorting in-memory based on computed counts
        switch (normalized) {
            case "mostliked":
                theoryDTOs.sort((a, b) -> Integer.compare(computeVotesPercentage(b.getUpVotesCount(), b.getDownVotesCount()), computeVotesPercentage(a.getUpVotesCount(), a.getDownVotesCount())));
                break;
            case "mostvotes":
                theoryDTOs.sort((a, b) -> Integer.compare((b.getUpVotesCount() + b.getDownVotesCount()), (a.getUpVotesCount() + a.getDownVotesCount())));
                break;
            case "mostcomments":
                theoryDTOs.sort((a, b) -> Integer.compare(b.getCommentsCount(), a.getCommentsCount()));
                break;
            case "date":
            default:
                // already sorted by id desc from query
                break;
        }
        
        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
        
        return Response.builder()
                .status(200)
                .message("success")
                .theories(theoryDTOs)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .build();
    }

    @Override
    public Response getTheoryById(Long id) {
        Theory theory = theoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Theory Not Found"));

        TheoryDTO theoryDTO = modelMapper.map(theory, TheoryDTO.class);
        if (theoryDTO.getUser() != null) {
            theoryDTO.getUser().setTheories(null);
            theoryDTO.getUser().setComments(null);
            theoryDTO.getUser().setEmail(null);
            theoryDTO.getUser().setRole(null);
            theoryDTO.getUser().setCreatedAt(null);
        }
        
        // Force aiScore mapping if not set
        if (theoryDTO.getAiScore() == null) {
            theoryDTO.setAiScore(theory.getAiScore() != null ? theory.getAiScore() : 50);
        }

        Response commentsResponse = commentService.getAllTheoryComments(theoryDTO.getId());
        Response upVotesResponse = voteService.getAllTheoryUpVotes(theoryDTO.getId());
        Response downVotesResponse = voteService.getAllTheoryDownVotes(theoryDTO.getId());
        theoryDTO.setCommentsCount(countCommentsRecursive(commentsResponse.getComments()));
        theoryDTO.setUpVotesCount(upVotesResponse.getVotes().size());
        theoryDTO.setDownVotesCount(downVotesResponse.getVotes().size());
        // aiScore is already mapped from the entity

        return Response.builder()
                .status(200)
                .message("success")
                .theory(theoryDTO)
                .build();
    }

    @Override
    public Response getTheoryVersions(Long id) {
        Theory theory = theoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Theory Not Found"));
        List<TheoryVersion> versions = theoryVersionRepository.findByTheoryOrderByVersionNumberDesc(theory);
        List<com.example.OnePieceTheoryEvaluator.dto.TheoryVersionDTO> dtos = modelMapper.map(versions, new TypeToken<List<com.example.OnePieceTheoryEvaluator.dto.TheoryVersionDTO>>() {}.getType());
        return Response.builder()
                .status(200)
                .message("success")
                .versions(dtos)
                .build();
    }

    @Override
    public Response getTheoryByUserId(Long id) {
        List<Theory> theories = theoryRepository.findByUserIdOrderByIdDesc(id);
        List<TheoryDTO> theoryDTOs = modelMapper.map(theories, new TypeToken<List<TheoryDTO>>() {}.getType());

        theoryDTOs.forEach(t -> t.setUser(null)); // Do not return the user 

        return Response.builder()
                .status(200)
                .message("success")
                .theories(theoryDTOs)
                .build();
    }

    @Override
    public Response getTrendingTheories(int limit) {
        List<Theory> theories = theoryRepository.findTrendingTheories(limit);
        List<TheoryDTO> theoryDTOs = modelMapper.map(theories, new TypeToken<List<TheoryDTO>>() {}.getType());

        // Sanitize user info like in getAllTheories
        theoryDTOs.forEach(t -> {
            UserDTO userDTO = t.getUser();
            if (userDTO != null) {
                userDTO.setEmail(null);
                userDTO.setRole(null);
                userDTO.setCreatedAt(null);
                userDTO.setTheories(null);
                userDTO.setComments(null);
            }
        });

        // Fetch comments count and votes count for each theory
        for (TheoryDTO t : theoryDTOs) {
            Response commentsResponse = commentService.getAllTheoryComments(t.getId());
            Response votesResponse = voteService.getAllTheoryUpVotes(t.getId());
            Response downVotesResponse = voteService.getAllTheoryDownVotes(t.getId());
            t.setCommentsCount(countCommentsRecursive(commentsResponse.getComments()));
            t.setUpVotesCount(votesResponse.getVotes().size());
            t.setDownVotesCount(downVotesResponse.getVotes().size());
            
            // Force aiScore mapping if not set
            if (t.getAiScore() == null) {
                Theory theory = theoryRepository.findById(t.getId()).orElse(null);
                t.setAiScore(theory != null && theory.getAiScore() != null ? theory.getAiScore() : 50);
            }
        }

        return Response.builder()
                .status(200)
                .message("success")
                .theories(theoryDTOs)
                .build();
    }

    @Override
    public Response getLastChapterTheories(int limit) {
        List<Theory> theories = theoryRepository.findLastChapterTheories(limit);
        List<TheoryDTO> theoryDTOs = modelMapper.map(theories, new TypeToken<List<TheoryDTO>>() {}.getType());

        // Sanitize user info like in getAllTheories
        theoryDTOs.forEach(t -> {
            UserDTO userDTO = t.getUser();
            if (userDTO != null) {
                userDTO.setEmail(null);
                userDTO.setRole(null);
                userDTO.setCreatedAt(null);
                userDTO.setTheories(null);
                userDTO.setComments(null);
            }
        });

        // Fetch comments count and votes count for each theory
        for (TheoryDTO t : theoryDTOs) {
            Response commentsResponse = commentService.getAllTheoryComments(t.getId());
            Response votesResponse = voteService.getAllTheoryUpVotes(t.getId());
            Response downVotesResponse = voteService.getAllTheoryDownVotes(t.getId());
            t.setCommentsCount(countCommentsRecursive(commentsResponse.getComments()));
            t.setUpVotesCount(votesResponse.getVotes().size());
            t.setDownVotesCount(downVotesResponse.getVotes().size());
            
            // Force aiScore mapping if not set
            if (t.getAiScore() == null) {
                Theory theory = theoryRepository.findById(t.getId()).orElse(null);
                t.setAiScore(theory != null && theory.getAiScore() != null ? theory.getAiScore() : 50);
            }
        }

        return Response.builder()
                .status(200)
                .message("success")
                .theories(theoryDTOs)
                .build();
    }

    private String saveImage(MultipartFile imageFile){
        //create the directory to store images if it doesn't exist
        File directory = new File(imageDirectory);

        if (!directory.exists()){
            directory.mkdirs();
            log.info("Directory was created: {}", imageDirectory);
        }
        
        // Sanitize filename for security
        String sanitizedFilename = fileUploadSecurityService.sanitizeFilename(imageFile.getOriginalFilename());
        String uniqueFileName = UUID.randomUUID() + "_" + sanitizedFilename;
        
        //get the absolute path of the image
        String imagePath = imageDirectory + (imageDirectory.endsWith("/") ? "" : "/") + uniqueFileName;

        try {
            File destinationFile = new File(imagePath);
            imageFile.transferTo(destinationFile); //we are transfering(writing to this folder)
            log.info("Image saved successfully: {}", uniqueFileName);

        }catch (Exception e){
            log.error("Error occurred while saving image: {}", e.getMessage());
            throw new IllegalArgumentException("Error occurred while saving image: " + e.getMessage());
        }

        return uniqueFileName;
    }

    private int countCommentsRecursive(List<com.example.OnePieceTheoryEvaluator.dto.CommentDTO> roots){
        if (roots == null) return 0;
        int total = 0;
        for (com.example.OnePieceTheoryEvaluator.dto.CommentDTO c : roots){
            total += 1;
            if (c.getReplies() != null && !c.getReplies().isEmpty()){
                total += countCommentsRecursive(c.getReplies());
            }
        }
        return total;
    }
    
    private int computeVotesPercentage(int upVotesCount, int downVotesCount){
        if (upVotesCount + downVotesCount == 0) return 0;
        return (upVotesCount * 100 / (upVotesCount + downVotesCount)) ; // dont forget the * 100 otherwise it will return mostly 0 or 1
    }


  
    public static String toSlug(String input) {
      int extensionIndex = input.lastIndexOf(".");
      String extension = input.substring(extensionIndex, input.length());
      String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
      String normalized = Normalizer.normalize(nowhitespace, Form.NFD);
      String slug = NONLATIN.matcher(normalized).replaceAll("");
      return slug.toLowerCase(Locale.ENGLISH) + extension;
      
    }
}
