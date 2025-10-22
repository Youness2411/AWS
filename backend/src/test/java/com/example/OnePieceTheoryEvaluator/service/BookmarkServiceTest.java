package com.example.OnePieceTheoryEvaluator.service;

import com.example.OnePieceTheoryEvaluator.dto.BookmarkDTO;
import com.example.OnePieceTheoryEvaluator.dto.Response;
import com.example.OnePieceTheoryEvaluator.entity.Bookmark;
import com.example.OnePieceTheoryEvaluator.entity.Theory;
import com.example.OnePieceTheoryEvaluator.entity.User;
import com.example.OnePieceTheoryEvaluator.enums.UserRole;
import com.example.OnePieceTheoryEvaluator.exceptions.NotFoundException;
import com.example.OnePieceTheoryEvaluator.repository.BookmarkRepository;
import com.example.OnePieceTheoryEvaluator.repository.TheoryRepository;
import com.example.OnePieceTheoryEvaluator.service.impl.BookmarkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private TheoryRepository theoryRepository;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private BookmarkServiceImpl bookmarkService;

    private User testUser;
    private Theory testTheory;
    private Bookmark testBookmark;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .role(UserRole.USER)
                .build();

        // Setup test theory
        testTheory = Theory.builder()
                .id(1L)
                .title("Test Theory")
                .content("Test content")
                .user(testUser)
                .build();

        // Setup test bookmark
        testBookmark = Bookmark.builder()
                .id(1L)
                .user(testUser)
                .theory(testTheory)
                .build();
    }

    @Test
    void addBookmark_Success() {
        // Arrange
        BookmarkDTO bookmarkDTO = new BookmarkDTO();
        bookmarkDTO.setTheoryId(1L);

        when(userService.getCurrentLoggedInUser()).thenReturn(testUser);
        when(theoryRepository.findById(1L)).thenReturn(Optional.of(testTheory));
        when(bookmarkRepository.existsByUserIdAndTheoryId(1L, 1L)).thenReturn(false);
        when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(testBookmark);

        // Act
        Response response = bookmarkService.addBookmark(bookmarkDTO);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("Theory bookmarked successfully", response.getMessage());
        verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
    }

    @Test
    void addBookmark_AlreadyBookmarked() {
        // Arrange
        BookmarkDTO bookmarkDTO = new BookmarkDTO();
        bookmarkDTO.setTheoryId(1L);

        when(userService.getCurrentLoggedInUser()).thenReturn(testUser);
        when(theoryRepository.findById(1L)).thenReturn(Optional.of(testTheory));
        when(bookmarkRepository.existsByUserIdAndTheoryId(1L, 1L)).thenReturn(true);

        // Act
        Response response = bookmarkService.addBookmark(bookmarkDTO);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("Theory already bookmarked", response.getMessage());
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    void addBookmark_TheoryNotFound() {
        // Arrange
        BookmarkDTO bookmarkDTO = new BookmarkDTO();
        bookmarkDTO.setTheoryId(999L);

        when(userService.getCurrentLoggedInUser()).thenReturn(testUser);
        when(theoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            bookmarkService.addBookmark(bookmarkDTO);
        });
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    void addBookmark_MissingTheoryId() {
        // Arrange
        BookmarkDTO bookmarkDTO = new BookmarkDTO();
        // theoryId is null

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            bookmarkService.addBookmark(bookmarkDTO);
        });
    }

    @Test
    void removeBookmark_Success() {
        // Arrange
        when(userService.getCurrentLoggedInUser()).thenReturn(testUser);
        when(bookmarkRepository.findByUserIdAndTheoryId(1L, 1L))
                .thenReturn(Optional.of(testBookmark));
        doNothing().when(bookmarkRepository).delete(any(Bookmark.class));

        // Act
        Response response = bookmarkService.removeBookmark(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("Bookmark removed successfully", response.getMessage());
        verify(bookmarkRepository, times(1)).delete(testBookmark);
    }

    @Test
    void removeBookmark_NotFound() {
        // Arrange
        when(userService.getCurrentLoggedInUser()).thenReturn(testUser);
        when(bookmarkRepository.findByUserIdAndTheoryId(1L, 999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            bookmarkService.removeBookmark(999L);
        });
        verify(bookmarkRepository, never()).delete(any(Bookmark.class));
    }

    @Test
    void getUserBookmarks_Success() {
        // Arrange
        List<Bookmark> bookmarks = new ArrayList<>();
        bookmarks.add(testBookmark);

        when(userService.getCurrentLoggedInUser()).thenReturn(testUser);
        when(bookmarkRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(bookmarks);
        when(modelMapper.map(any(Theory.class), any())).thenReturn(new com.example.OnePieceTheoryEvaluator.dto.TheoryDTO());

        // Act
        Response response = bookmarkService.getUserBookmarks();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getTheories());
        assertEquals(1, response.getTheories().size());
    }

    @Test
    void getUserBookmarks_Empty() {
        // Arrange
        when(userService.getCurrentLoggedInUser()).thenReturn(testUser);
        when(bookmarkRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(new ArrayList<>());

        // Act
        Response response = bookmarkService.getUserBookmarks();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertNotNull(response.getTheories());
        assertTrue(response.getTheories().isEmpty());
    }

    @Test
    void isBookmarked_True() {
        // Arrange
        when(userService.getCurrentLoggedInUser()).thenReturn(testUser);
        when(bookmarkRepository.existsByUserIdAndTheoryId(1L, 1L)).thenReturn(true);

        // Act
        Response response = bookmarkService.isBookmarked(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("bookmarked", response.getMessage());
    }

    @Test
    void isBookmarked_False() {
        // Arrange
        when(userService.getCurrentLoggedInUser()).thenReturn(testUser);
        when(bookmarkRepository.existsByUserIdAndTheoryId(1L, 1L)).thenReturn(false);

        // Act
        Response response = bookmarkService.isBookmarked(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("not bookmarked", response.getMessage());
    }

    @Test
    void getBookmarkCount_Success() {
        // Arrange
        when(bookmarkRepository.countByTheoryId(1L)).thenReturn(5L);

        // Act
        Response response = bookmarkService.getBookmarkCount(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        verify(bookmarkRepository, times(1)).countByTheoryId(1L);
    }

    @Test
    void getTheoryBookmarks_Success() {
        // Arrange
        List<Bookmark> bookmarks = new ArrayList<>();
        bookmarks.add(testBookmark);

        when(bookmarkRepository.findByTheoryIdOrderByCreatedAtDesc(1L)).thenReturn(bookmarks);
        when(modelMapper.map(any(User.class), any())).thenReturn(new com.example.OnePieceTheoryEvaluator.dto.UserDTO());

        // Act
        Response response = bookmarkService.getTheoryBookmarks(1L);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("success", response.getMessage());
    }
}

