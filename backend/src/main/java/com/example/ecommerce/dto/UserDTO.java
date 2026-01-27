package com.example.ecommerce.dto;

import com.example.ecommerce.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String username;
    private String name;
    private String representativePhone;
    private String email;
    private User.Role role;
    private String businessNumber; // user 테이블의 businessNumber
    private boolean isActive;
    private LocalDateTime createdAt;

    // Main Business Profile 정보 (Option)
    private String companyName; // business_profiles.business_name
    private String officeAddress;
    private String businessStatus; // business_profiles.status
}