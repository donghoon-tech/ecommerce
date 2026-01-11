package com.example.ecommerce.repository;

import com.example.ecommerce.entity.RegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, UUID> {
    List<RegistrationRequest> findByStatus(String status);
}
