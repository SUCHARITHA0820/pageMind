package com.pagemind.book;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "books", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_title_author", columnNames = {"title", "author"})
    },
    indexes = {
        @Index(name = "idx_books_title", columnList = "title"),
        @Index(name = "idx_books_genre", columnList = "genre")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(length = 100)
    private String genre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @Column(name = "buy_links_json", columnDefinition = "JSON")
    private String buyLinksJson;

    @Column(name = "published_year")
    private Integer publishedYear;

    @Column(name = "rating", columnDefinition = "DECIMAL(2,1)")
    private Double rating;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
