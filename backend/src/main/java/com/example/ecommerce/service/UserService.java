package com.example.ecommerce.service;

import com.example.ecommerce.dto.UserDTO;
import com.example.ecommerce.dto.UserUpdateRequest;
import com.example.ecommerce.entity.BusinessProfile;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.BusinessProfileRepository;
import com.example.ecommerce.repository.RoleRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.example.ecommerce.mapper.UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public UserDTO getMyInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 메인 사업자 프로필 조회 (없을 수도 있음 - 가입 초기 등)
        BusinessProfile mainProfile = businessProfileRepository.findByUserId(user.getId()).stream()
                .filter(BusinessProfile::isMain)
                .findFirst()
                .orElse(null);

        return userMapper.toDTO(user, mainProfile);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserDetail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        BusinessProfile mainProfile = businessProfileRepository.findByUserId(user.getId()).stream()
                .filter(BusinessProfile::isMain)
                .findFirst()
                .orElse(null);

        return userMapper.toDTO(user, mainProfile);
    }

    public UserDTO updateMyInfo(String username, UserUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 유저 기본 정보 수정
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getName() != null)
            user.setName(request.getName());
        if (request.getRepresentativePhone() != null)
            user.setRepresentativePhone(request.getRepresentativePhone());
        if (request.getEmail() != null)
            user.setEmail(request.getEmail());

        // 사업자 정보 수정 (메인 프로필이 존재할 경우)
        if (request.getCompanyName() != null || request.getOfficeAddress() != null) {
            businessProfileRepository.findByUserId(user.getId()).stream()
                    .filter(BusinessProfile::isMain)
                    .findFirst()
                    .ifPresent(profile -> {
                        if (request.getCompanyName() != null)
                            profile.setBusinessName(request.getCompanyName());
                        if (request.getOfficeAddress() != null)
                            profile.setOfficeAddress(request.getOfficeAddress());
                        if (request.getStorageAddress() != null)
                            profile.setStorageAddress(request.getStorageAddress());
                    });
        }

        return getMyInfo(username); // 수정된 정보 반환
    }

    /**
     * 관리자: 전체 사용자 목록 조회 (BusinessProfile 포함)
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersWithProfiles() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> {
                    BusinessProfile mainProfile = businessProfileRepository.findByUserId(user.getId()).stream()
                            .filter(BusinessProfile::isMain)
                            .findFirst()
                            .orElse(null);
                    return userMapper.toDTO(user, mainProfile);
                })
                .collect(Collectors.toList());
    }

    /**
     * 관리자: 사용자 역할 변경
     */
    public UserDTO updateUserRole(UUID userId, String roleStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Role newRole = roleRepository.findByName(roleStr.toUpperCase())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 권한입니다."));
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());

        BusinessProfile mainProfile = businessProfileRepository.findByUserId(user.getId()).stream()
                .filter(BusinessProfile::isMain)
                .findFirst()
                .orElse(null);

        return userMapper.toDTO(user, mainProfile);
    }

    /**
     * 관리자: 사업자 프로필 승인
     */
    public void approveBusinessProfile(UUID profileId, String token) {
        BusinessProfile profile = businessProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("프로필을 찾을 수 없습니다."));

        // JWT에서 관리자 username 추출
        String adminUsername = jwtTokenProvider.getUsername(token.replace("Bearer ", ""));
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        // 1. 프로필 상태 변경
        profile.setStatus(BusinessProfile.Status.approved);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setApprovedBy(admin);
        profile.setRejectionReason(null);
        profile.setUpdatedAt(LocalDateTime.now());

        // 2. 사용자 역할 변경 (UNVERIFIED -> USER)
        User user = profile.getUser();
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER 역할을 찾을 수 없습니다."));
        user.setRole(userRole);
    }

    /**
     * 관리자: 사업자 프로필 반려
     */
    public void rejectBusinessProfile(UUID profileId, String reason) {
        BusinessProfile profile = businessProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("프로필을 찾을 수 없습니다."));

        profile.setStatus(BusinessProfile.Status.rejected);
        profile.setRejectionReason(reason);
        profile.setUpdatedAt(LocalDateTime.now());
    }
}
