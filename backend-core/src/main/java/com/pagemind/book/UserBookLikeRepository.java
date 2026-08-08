package com.pagemind.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookLikeRepository extends JpaRepository<UserBookLike, Long> {

    List<UserBookLike> findByUserId(Long userId);

    Optional<UserBookLike> findByUserIdAndBookId(Long userId, Long bookId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
