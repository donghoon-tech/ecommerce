package com.example.ecommerce.repository;

import com.example.ecommerce.entity.BusinessLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BusinessLicenseRepository extends JpaRepository<BusinessLicense, UUID> {
    List<BusinessLicense> findByMemberId(UUID memberId);
}
