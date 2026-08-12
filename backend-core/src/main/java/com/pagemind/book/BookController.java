package com.pagemind.book;

import com.pagemind.auth.dto.ApiResponse;
import com.pagemind.book.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({"/api", "/v1"})
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/books")
    public ResponseEntity<PageResponse<Book>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> booksPage = bookService.getAllBooks(genre, search, pageable);
        return ResponseEntity.ok(PageResponse.from(booksPage));
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        Optional<Book> bookOpt = bookService.getBookById(id);
        if (bookOpt.isPresent()) {
            return ResponseEntity.ok(bookOpt.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "Book not found with id: " + id));
    }

    @PostMapping("/user/likes/{bookId}")
    public ResponseEntity<?> likeBook(@PathVariable Long bookId, Principal principal) {
        System.out.println("[BookController] POST /user/likes/" + bookId + " called by principal: " + (principal != null ? principal.getName() : "NULL"));
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        try {
            bookService.likeBook(principal.getName(), bookId);
            System.out.println("[BookController] POST /user/likes/" + bookId + " succeeded for " + principal.getName());
            return ResponseEntity.ok(new ApiResponse(true, "Book liked successfully."));
        } catch (IllegalArgumentException e) {
            System.err.println("[BookController] POST /user/likes/" + bookId + " failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/user/likes/{bookId}")
    public ResponseEntity<?> unlikeBook(@PathVariable Long bookId, Principal principal) {
        System.out.println("[BookController] DELETE /user/likes/" + bookId + " called by principal: " + (principal != null ? principal.getName() : "NULL"));
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        try {
            bookService.unlikeBook(principal.getName(), bookId);
            System.out.println("[BookController] DELETE /user/likes/" + bookId + " succeeded for " + principal.getName());
            return ResponseEntity.ok(new ApiResponse(true, "Book unliked successfully."));
        } catch (IllegalArgumentException e) {
            System.err.println("[BookController] DELETE /user/likes/" + bookId + " failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/user/likes")
    public ResponseEntity<?> getUserLikes(Principal principal) {
        System.out.println("[BookController] GET /user/likes called by principal: " + (principal != null ? principal.getName() : "NULL"));
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required."));
        }

        try {
            List<Book> likedBooks = bookService.getUserLikedBooks(principal.getName());
            System.out.println("[BookController] GET /user/likes returning " + likedBooks.size() + " books for " + principal.getName());
            return ResponseEntity.ok(likedBooks);
        } catch (IllegalArgumentException e) {
            System.err.println("[BookController] GET /user/likes failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
