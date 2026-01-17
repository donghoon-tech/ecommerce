package com.example.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private UUID id;
    private String username;
    private String role;
    
    private String companyName;
    private String phone;
    private String email;
    
    private String businessNumber;
    private String businessAddress;
    private String yardAddress;
}