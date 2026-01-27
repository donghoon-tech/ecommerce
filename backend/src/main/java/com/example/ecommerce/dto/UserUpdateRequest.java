package com.example.ecommerce.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String password;
    private String name;
    private String representativePhone;
    private String email;

    // 사업자 정보 수정 요청
    private String companyName;
    private String officeAddress;
    private String storageAddress;
}
