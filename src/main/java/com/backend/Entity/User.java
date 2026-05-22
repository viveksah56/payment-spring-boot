package com.backend.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;


    @Column(unique = true, nullable = false)
    private String email;

    private String password;


    @OneToMany(mappedBy = "user")
    private List<Payment> payments = new ArrayList<>();


}
