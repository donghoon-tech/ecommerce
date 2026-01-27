package com.example.ecommerce.service;

import com.example.ecommerce.controller.AuthController;
import com.example.ecommerce.dto.UserDTO;
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
public class AuthService {
    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.example.ecommerce.mapper.UserMapper userMapper;

    public UserDTO register(AuthController.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.existsByRepresentativePhone(request.getPhone())) {
            throw new RuntimeException("이미 가입된 전화번호입니다.");
        }

        // 1. 사용자 계정 생성
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getCompanyName()) // 초기값: 상호명을 이름으로 사용? or 별도 이름 필드 필요? (일단 DTO 확인 필요) -> 아 request 필드 보니
                                                // name이 없었음.
                .representativePhone(request.getPhone())
                .email(request.getEmail())
                .role(User.Role.user)
                .businessNumber(request.getBusinessNumber())
                .isActive(true)
                .build();

        // request에 name(대표자명) 필드가 없다면, 회원가입 폼을 수정해야 할 수도 있음.
        // 하지만 기존 코드: companyName을 사용.
        // 새 스키마 User.name (대표자명), BusinessProfile.businessName (상호명),
        // BusinessProfile.representativeName (대표자명)
        // 일단 request의 companyName을 상호명으로, User.name은 '대표자'로 가정하고 매핑하거나, AuthController
        // 수정이 필요해 보임.
        // 여기서는 임시로 companyName을 User.name과 BusinessProfile.businessName 둘 다에 넣겠습니다.
        // (정확한 매핑을 위해선 AuthController.RegisterRequest 수정 필요)

        user.setName(request.getCompanyName()); // 임시

        User savedUser = userRepository.save(user);

        // 2. 사업자 프로필 생성 (Pending 상태)
        BusinessProfile profile = BusinessProfile.builder()
                .user(savedUser)
                .businessName(request.getCompanyName())
                .businessNumber(request.getBusinessNumber())
                .representativeName(request.getCompanyName()) // 대표자명 필드 부재로 상호명 사용
                .officeAddress(request.getBusinessAddress())
                .storageAddress(request.getYardAddress())
                .status(BusinessProfile.Status.pending)
                .isMain(true) // 첫 가입 시 주 사업자로 설정
                .build();

        businessProfileRepository.save(profile);

        // 3. 반환
        return userMapper.toDTO(savedUser, profile);
    }

    public String findId(String phone) {
        return userRepository.findByRepresentativePhone(phone)
                .map(User::getUsername)
                .orElseThrow(() -> new RuntimeException("해당 번호로 가입된 아이디가 없습니다."));
    }

    public String resetPassword(String username, String phone) {
        User user = userRepository.findByUsername(username)
                .filter(u -> u.getRepresentativePhone().equals(phone))
                .orElseThrow(() -> new RuntimeException("일치하는 회원 정보가 없습니다."));

        String tempPassword = generateRandomPassword();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        // Dirty Checking

        return tempPassword;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}