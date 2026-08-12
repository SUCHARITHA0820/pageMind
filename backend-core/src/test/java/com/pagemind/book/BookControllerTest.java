package com.pagemind.book;

import com.pagemind.book.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @Mock
    private Principal principal;

    @InjectMocks
    private BookController bookController;

    private Book sampleBook1;
    private Book sampleBook2;

    @BeforeEach
    void setUp() {
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
    void getAllBooks_returnsPaginatedResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        when(bookService.getAllBooks(null, null, pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(sampleBook1, sampleBook2), pageable, 2));

        ResponseEntity<PageResponse<Book>> response = bookController.getAllBooks(0, 20, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(2, response.getBody().getTotalElements());
        assertEquals(1, response.getBody().getTotalPages());
        assertEquals(0, response.getBody().getCurrentPage());
    }

    @Test
    void getAllBooks_withGenreAndSearch_returnsFilteredPaginatedBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookService.getAllBooks("Sci-Fi", "Dune", pageable))
                .thenReturn(new PageImpl<>(Collections.singletonList(sampleBook1), pageable, 1));

        ResponseEntity<PageResponse<Book>> response = bookController.getAllBooks(0, 10, "Sci-Fi", "Dune");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Dune", response.getBody().getContent().get(0).getTitle());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(1, response.getBody().getTotalPages());
        assertEquals(0, response.getBody().getCurrentPage());
    }

    @Test
    void getBookById_whenFound_returns200() {
        when(bookService.getBookById(101L)).thenReturn(Optional.of(sampleBook1));

        ResponseEntity<?> response = bookController.getBookById(101L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Book);
        assertEquals("Dune", ((Book) response.getBody()).getTitle());
    }

    @Test
    void getBookById_whenNotFound_returns404() {
        when(bookService.getBookById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = bookController.getBookById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void likeBook_authenticated_returns200() {
        when(principal.getName()).thenReturn("alice@example.com");
        when(bookService.likeBook("alice@example.com", 101L)).thenReturn(true);

        ResponseEntity<?> response = bookController.likeBook(101L, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).likeBook("alice@example.com", 101L);
    }

    @Test
    void likeBook_unauthenticated_returns401() {
        ResponseEntity<?> response = bookController.likeBook(101L, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(bookService, never()).likeBook(anyString(), anyLong());
    }

    @Test
    void unlikeBook_authenticated_returns200() {
        when(principal.getName()).thenReturn("alice@example.com");
        when(bookService.unlikeBook("alice@example.com", 101L)).thenReturn(true);

        ResponseEntity<?> response = bookController.unlikeBook(101L, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).unlikeBook("alice@example.com", 101L);
    }

    @Test
    void unlikeBook_unauthenticated_returns401() {
        ResponseEntity<?> response = bookController.unlikeBook(101L, null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(bookService, never()).unlikeBook(anyString(), anyLong());
    }

    @Test
    void getUserLikes_authenticated_returnsLikedBooks() {
        when(principal.getName()).thenReturn("alice@example.com");
        when(bookService.getUserLikedBooks("alice@example.com")).thenReturn(Collections.singletonList(sampleBook1));

        ResponseEntity<?> response = bookController.getUserLikes(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        @SuppressWarnings("unchecked")
        List<Book> books = (List<Book>) response.getBody();
        assertEquals(1, books.size());
        assertEquals("Dune", books.get(0).getTitle());
    }

    @Test
    void getUserLikes_unauthenticated_returns401() {
        ResponseEntity<?> response = bookController.getUserLikes(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(bookService, never()).getUserLikedBooks(anyString());
    }
}
