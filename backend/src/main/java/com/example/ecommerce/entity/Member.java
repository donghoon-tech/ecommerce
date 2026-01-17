package com.example.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false) // 논리적 Unique
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private String role = "USER";

    // 사용자 정보
    @Column(name = "company_name")
    private String companyName;

    @Column(nullable = false)
    private String phone;

    private String email;

    // 사업자 정보
    @Column(name = "business_number")
    private String businessNumber;

    @Column(name = "business_address")
    private String businessAddress;

    @Column(name = "yard_address")
    private String yardAddress;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}