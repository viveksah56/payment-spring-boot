package com.backend.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends AuditEntity{


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;


    private String orderId;

    private Double amount;

    private String currency;

    private String provider;

    private String status;

    private String customerEmail;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
