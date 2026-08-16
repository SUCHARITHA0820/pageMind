package com.pagemind.book;

import com.pagemind.user.User;
import com.pagemind.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserBookLikeRepository userBookLikeRepository;
    private final UserRepository userRepository;

    public Page<Book> getAllBooks(String genre, String search, Pageable pageable) {
        boolean hasGenre = StringUtils.hasText(genre);
        boolean hasSearch = StringUtils.hasText(search);

        if (hasGenre && hasSearch) {
            return bookRepository.findByGenreIgnoreCaseAndSearch(genre.trim(), search.trim(), pageable);
        } else if (hasSearch) {
            String term = search.trim();
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(term, term, pageable);
        } else if (hasGenre) {
            return bookRepository.findByGenreIgnoreCase(genre.trim(), pageable);
        } else {
            return bookRepository.findAll(pageable);
        }
    }

    public List<Book> getAllBooks(String genre, String search) {
        if (StringUtils.hasText(search)) {
            String term = search.trim();
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(term, term);
        }
        if (StringUtils.hasText(genre)) {
            return bookRepository.findByGenreIgnoreCase(genre.trim());
        }
        return bookRepository.findAll();
    }

    public List<Book> getAllBooks(String genre) {
        return getAllBooks(genre, null);
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    @Transactional
    public boolean likeBook(String userEmail, Long bookId) {
        System.out.println("[BookService] likeBook() called for userEmail=" + userEmail + ", bookId=" + bookId);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));

        System.out.println("[BookService] Resolved user ID: " + user.getId() + " (" + user.getEmail() + "), book ID: " + book.getId() + " (" + book.getTitle() + ")");

        if (userBookLikeRepository.existsByUserIdAndBookId(user.getId(), book.getId())) {
            System.out.println("[BookService] User ID " + user.getId() + " already liked book ID " + book.getId());
            return true;
        }

        UserBookLike like = UserBookLike.builder()
                .user(user)
                .book(book)
                .build();

        UserBookLike savedLike = userBookLikeRepository.save(like);
        Long savedId = (savedLike != null) ? savedLike.getId() : null;
        System.out.println("[BookService] Successfully saved UserBookLike row ID: " + savedId + " (userId=" + user.getId() + ", bookId=" + book.getId() + ")");
        return true;
    }

    @Transactional
    public boolean unlikeBook(String userEmail, Long bookId) {
        System.out.println("[BookService] unlikeBook() called for userEmail=" + userEmail + ", bookId=" + bookId);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        if (!bookRepository.existsById(bookId)) {
            throw new IllegalArgumentException("Book not found with id: " + bookId);
        }

        System.out.println("[BookService] Deleting UserBookLike row for userId=" + user.getId() + ", bookId=" + bookId);
        userBookLikeRepository.deleteByUserIdAndBookId(user.getId(), bookId);
        System.out.println("[BookService] Successfully unliked book ID " + bookId + " for user ID " + user.getId());
        return true;
    }

    @Transactional(readOnly = true)
    public List<Book> getUserLikedBooks(String userEmail) {
        System.out.println("[BookService] getUserLikedBooks() called for userEmail=" + userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        System.out.println("[BookService] Querying liked books from repository for user ID: " + user.getId() + " (" + user.getEmail() + ")");
        List<Book> likedBooks = userBookLikeRepository.findLikedBooksByUserId(user.getId());

        if (likedBooks == null || likedBooks.isEmpty()) {
            System.out.println("[BookService] No liked books found in database for user ID: " + user.getId());
            return Collections.emptyList();
        }

        System.out.println("[BookService] Found " + likedBooks.size() + " liked books for user ID " + user.getId() + ": " +
                likedBooks.stream().map(b -> b.getId() + ":" + b.getTitle()).collect(Collectors.joining(", ")));

        return likedBooks;
    }
}
