package com.example.ecommerce.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ProfileDTO {
    private UUID id;
    private String name;
    private String email;
    private String phone;
}
