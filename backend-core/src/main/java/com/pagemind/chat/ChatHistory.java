package com.pagemind.chat;

import com.pagemind.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_history", indexes = {
    @Index(name = "idx_chat_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_chat_user"))
    private User user;

    @Column(name = "mood_input", columnDefinition = "TEXT")
    private String moodInput;

    @Column(name = "detected_genre", length = 100)
    private String detectedGenre;

    @Column(name = "recommended_books_json", columnDefinition = "JSON")
    private String recommendedBooksJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
