package com.example.OnePieceTheoryEvaluator.service.impl;

import com.example.OnePieceTheoryEvaluator.dto.BookmarkDTO;
import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.dto.TheoryDTO;
import com.example.OnePieceTheoryEvaluator.dto.UserDTO;
import com.example.OnePieceTheoryEvaluator.entity.Bookmark;
import com.example.OnePieceTheoryEvaluator.entity.Theory;
import com.example.OnePieceTheoryEvaluator.entity.User;
import com.example.OnePieceTheoryEvaluator.exceptions.NotFoundException;
import com.example.OnePieceTheoryEvaluator.repository.BookmarkRepository;
import com.example.OnePieceTheoryEvaluator.repository.TheoryRepository;
import com.example.OnePieceTheoryEvaluator.service.BookmarkService;
import com.example.OnePieceTheoryEvaluator.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final TheoryRepository theoryRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response addBookmark(BookmarkDTO bookmarkDTO) {
        if (bookmarkDTO.getTheoryId() == null) {
            throw new NotFoundException("Theory ID is required");
        }

        User currentUser = userService.getCurrentLoggedInUser();
        Theory theory = theoryRepository.findById(bookmarkDTO.getTheoryId())
                .orElseThrow(() -> new NotFoundException("Theory Not Found"));

        // Check if bookmark already exists
        if (bookmarkRepository.existsByUserIdAndTheoryId(currentUser.getId(), theory.getId())) {
            return Response.builder()
                    .status(200)
                    .message("Theory already bookmarked")
                    .build();
        }

        Bookmark bookmark = Bookmark.builder()
                .user(currentUser)
                .theory(theory)
                .build();

        bookmarkRepository.save(bookmark);

        log.info("User {} bookmarked theory {}", currentUser.getId(), theory.getId());

        return Response.builder()
                .status(200)
                .message("Theory bookmarked successfully")
                .build();
    }

    @Override
    @Transactional
    public Response removeBookmark(Long theoryId) {
        if (theoryId == null) {
            throw new NotFoundException("Theory ID is required");
        }

        User currentUser = userService.getCurrentLoggedInUser();

        Bookmark bookmark = bookmarkRepository.findByUserIdAndTheoryId(currentUser.getId(), theoryId)
                .orElseThrow(() -> new NotFoundException("Bookmark not found"));

        bookmarkRepository.delete(bookmark);

        log.info("User {} removed bookmark for theory {}", currentUser.getId(), theoryId);

        return Response.builder()
                .status(200)
                .message("Bookmark removed successfully")
                .build();
    }

    @Override
    public Response getUserBookmarks() {
        User currentUser = userService.getCurrentLoggedInUser();
        List<Bookmark> bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());

        List<TheoryDTO> theoryDTOs = new ArrayList<>();
        
        for (Bookmark bookmark : bookmarks) {
            Theory theory = bookmark.getTheory();
            if (theory != null) {
                TheoryDTO theoryDTO = modelMapper.map(theory, TheoryDTO.class);
                
                // Sanitize user info
                if (theoryDTO.getUser() != null) {
                    theoryDTO.getUser().setEmail(null);
                    theoryDTO.getUser().setRole(null);
                    theoryDTO.getUser().setCreatedAt(null);
                    theoryDTO.getUser().setTheories(null);
                    theoryDTO.getUser().setComments(null);
                }
                
                theoryDTOs.add(theoryDTO);
            }
        }

        return Response.builder()
                .status(200)
                .message("success")
                .theories(theoryDTOs)
                .build();
    }

    @Override
    public Response isBookmarked(Long theoryId) {
        if (theoryId == null) {
            throw new NotFoundException("Theory ID is required");
        }

        User currentUser = userService.getCurrentLoggedInUser();
        boolean isBookmarked = bookmarkRepository.existsByUserIdAndTheoryId(currentUser.getId(), theoryId);

        return Response.builder()
                .status(200)
                .message(isBookmarked ? "bookmarked" : "not bookmarked")
                .build();
    }

    @Override
    public Response getTheoryBookmarks(Long theoryId) {
        if (theoryId == null) {
            throw new NotFoundException("Theory ID is required");
        }

        List<Bookmark> bookmarks = bookmarkRepository.findByTheoryIdOrderByCreatedAtDesc(theoryId);
        
        List<BookmarkDTO> bookmarkDTOs = bookmarks.stream().map(bookmark -> {
            BookmarkDTO dto = new BookmarkDTO();
            dto.setId(bookmark.getId());
            dto.setTheoryId(theoryId);
            dto.setCreatedAt(bookmark.getCreatedAt());
            
            // Add user info
            if (bookmark.getUser() != null) {
                UserDTO userDTO = modelMapper.map(bookmark.getUser(), UserDTO.class);
                userDTO.setEmail(null);
                userDTO.setRole(null);
                userDTO.setCreatedAt(null);
                userDTO.setTheories(null);
                userDTO.setComments(null);
                dto.setUser(userDTO);
            }
            
            return dto;
        }).collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("success")
                .build();
    }

    @Override
    public Response getBookmarkCount(Long theoryId) {
        if (theoryId == null) {
            throw new NotFoundException("Theory ID is required");
        }

        long count = bookmarkRepository.countByTheoryId(theoryId);

        return Response.builder()
                .status(200)
                .message("success")
                .build();
    }
}

