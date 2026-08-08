package com.pagemind.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    Optional<PasswordResetCode> findTopByUserIdAndCodeOrderByCreatedAtDesc(Long userId, String code);

    @Transactional
    @Modifying
    @Query("DELETE FROM PasswordResetCode p WHERE p.user.id = :userId")
    void deleteByUserId(Long userId);
}
