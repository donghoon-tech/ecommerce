package com.example.ecommerce.service;

import com.example.ecommerce.dto.UserDTO;
import com.example.ecommerce.dto.UserUpdateRequest;
import com.example.ecommerce.entity.BusinessProfile;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.BusinessProfileRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.example.ecommerce.mapper.UserMapper userMapper;

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
}
