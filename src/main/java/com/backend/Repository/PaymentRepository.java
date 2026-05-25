package com.backend.Repository;

import com.backend.Entity.Payment;
import com.backend.Enum.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByProviderOrderId(String providerOrderId);

    @Query("SELECT p FROM Payment p WHERE p.user.userId = :userId AND p.deleted = false ORDER BY p.createdAt DESC")
    Page<Payment> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.user.userId = :userId AND p.status = :status AND p.deleted = false")
    Page<Payment> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") PaymentStatus status, Pageable pageable);

    List<Payment> findByCustomerEmail(String customerEmail);
}