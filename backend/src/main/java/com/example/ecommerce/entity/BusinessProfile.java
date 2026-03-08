package com.example.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "business_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "business_name", nullable = true, length = 200)
    private String businessName;

    @Column(name = "business_number", nullable = true, length = 20)
    private String businessNumber;

    @Column(name = "representative_name", nullable = true, length = 100)
    private String representativeName;

    @Column(name = "office_address", nullable = true, columnDefinition = "TEXT")
    private String officeAddress;

    @Column(name = "storage_address", columnDefinition = "TEXT")
    private String storageAddress;

    @Column(name = "br_image_url", columnDefinition = "TEXT")
    private String brImageUrl;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.pending;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "is_main", nullable = false)
    @Builder.Default
    private boolean isMain = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Status {
        pending, approved, rejected
    }
}
