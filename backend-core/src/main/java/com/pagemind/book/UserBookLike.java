package com.pagemind.book;

import com.pagemind.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_book_likes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_book", columnNames = {"user_id", "book_id"})
    },
    indexes = {
        @Index(name = "idx_likes_user_id", columnList = "user_id"),
        @Index(name = "idx_likes_book_id", columnList = "book_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBookLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_likes_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_likes_book"))
    private Book book;

    @CreationTimestamp
    @Column(name = "liked_at", updatable = false)
    private LocalDateTime likedAt;
}
