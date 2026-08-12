package com.pagemind.book;

import com.pagemind.user.User;
import com.pagemind.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserBookLikeRepository userBookLikeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookService bookService;

    private User sampleUser;
    private Book sampleBook1;
    private Book sampleBook2;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .build();

        sampleBook1 = Book.builder()
                .id(101L)
                .title("Dune")
                .author("Frank Herbert")
                .genre("Sci-Fi")
                .build();

        sampleBook2 = Book.builder()
                .id(102L)
                .title("The Hobbit")
                .author("J.R.R. Tolkien")
                .genre("Fantasy")
                .build();
    }

    @Test
    void getAllBooks_paginated_withoutFilters_returnsAllBooksPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(bookRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(sampleBook1, sampleBook2), pageable, 2));

        Page<Book> result = bookService.getAllBooks(null, null, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        verify(bookRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllBooks_paginated_withGenre_returnsFilteredGenrePage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(bookRepository.findByGenreIgnoreCase("Sci-Fi", pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(sampleBook1), pageable, 1));

        Page<Book> result = bookService.getAllBooks("Sci-Fi", null, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Dune", result.getContent().get(0).getTitle());
        verify(bookRepository, times(1)).findByGenreIgnoreCase("Sci-Fi", pageable);
    }

    @Test
    void getAllBooks_paginated_withSearch_returnsFilteredSearchPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("Dune", "Dune", pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(sampleBook1), pageable, 1));

        Page<Book> result = bookService.getAllBooks(null, "Dune", pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Dune", result.getContent().get(0).getTitle());
        verify(bookRepository, times(1)).findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("Dune", "Dune", pageable);
    }

    @Test
    void getAllBooks_paginated_withGenreAndSearch_returnsCombinedFilteredPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(bookRepository.findByGenreIgnoreCaseAndSearch("Sci-Fi", "Dune", pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(sampleBook1), pageable, 1));

        Page<Book> result = bookService.getAllBooks("Sci-Fi", "Dune", pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Dune", result.getContent().get(0).getTitle());
        verify(bookRepository, times(1)).findByGenreIgnoreCaseAndSearch("Sci-Fi", "Dune", pageable);
    }

    @Test
    void getAllBooks_withoutGenre_returnsAllBooks() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(sampleBook1, sampleBook2));

        List<Book> result = bookService.getAllBooks(null);

        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findAll();
        verify(bookRepository, never()).findByGenreIgnoreCase(anyString());
    }

    @Test
    void getAllBooks_withGenre_returnsFilteredBooks() {
        when(bookRepository.findByGenreIgnoreCase("Sci-Fi")).thenReturn(Collections.singletonList(sampleBook1));

        List<Book> result = bookService.getAllBooks("Sci-Fi");

        assertEquals(1, result.size());
        assertEquals("Dune", result.get(0).getTitle());
        verify(bookRepository, times(1)).findByGenreIgnoreCase("Sci-Fi");
    }

    @Test
    void getBookById_whenExists_returnsBook() {
        when(bookRepository.findById(101L)).thenReturn(Optional.of(sampleBook1));

        Optional<Book> result = bookService.getBookById(101L);

        assertTrue(result.isPresent());
        assertEquals("Dune", result.get().getTitle());
    }

    @Test
    void getBookById_whenNotExists_returnsEmpty() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.getBookById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void likeBook_whenUserAndBookExist_savesLike() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(101L)).thenReturn(Optional.of(sampleBook1));
        when(userBookLikeRepository.existsByUserIdAndBookId(1L, 101L)).thenReturn(false);

        boolean success = bookService.likeBook("alice@example.com", 101L);

        assertTrue(success);
        verify(userBookLikeRepository, times(1)).save(any(UserBookLike.class));
    }

    @Test
    void likeBook_whenAlreadyLiked_doesNotDuplicate() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(101L)).thenReturn(Optional.of(sampleBook1));
        when(userBookLikeRepository.existsByUserIdAndBookId(1L, 101L)).thenReturn(true);

        boolean success = bookService.likeBook("alice@example.com", 101L);

        assertTrue(success);
        verify(userBookLikeRepository, never()).save(any(UserBookLike.class));
    }

    @Test
    void likeBook_whenBookNotFound_throwsException() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> bookService.likeBook("alice@example.com", 999L));
    }

    @Test
    void unlikeBook_whenBookExists_deletesLike() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(bookRepository.existsById(101L)).thenReturn(true);

        boolean success = bookService.unlikeBook("alice@example.com", 101L);

        assertTrue(success);
        verify(userBookLikeRepository, times(1)).deleteByUserIdAndBookId(1L, 101L);
    }

    @Test
    void unlikeBook_whenBookNotFound_throwsException() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(bookRepository.existsById(999L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> bookService.unlikeBook("alice@example.com", 999L));
    }

    @Test
    void getUserLikedBooks_returnsLikedBooksList() {
        UserBookLike like = UserBookLike.builder()
                .id(10L)
                .user(sampleUser)
                .book(sampleBook1)
                .build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(userBookLikeRepository.findByUserId(1L)).thenReturn(Collections.singletonList(like));

        List<Book> result = bookService.getUserLikedBooks("alice@example.com");

        assertEquals(1, result.size());
        assertEquals("Dune", result.get(0).getTitle());
    }
}
