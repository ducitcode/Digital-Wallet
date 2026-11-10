package com.example.user_service.repository;

import com.example.user_service.entity.TransactionPin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionPinRepository extends JpaRepository<TransactionPin, Long> {

    Optional<TransactionPin> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from TransactionPin p where p.userId = :userId")
    Optional<TransactionPin> findByUserIdForUpdate(@Param("userId") Long userId);
}
