package com.example.ecommerce.service;

import com.example.ecommerce.controller.AuthController;
import com.example.ecommerce.dto.MemberDTO;
import com.example.ecommerce.entity.Member;
import com.example.ecommerce.mapper.MemberMapper;
import com.example.ecommerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;

    public MemberDTO register(AuthController.RegisterRequest request) {
        if (memberRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        Member member = Member.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone()) // 필수
                .role("USER")
                // 선택 정보
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .businessNumber(request.getBusinessNumber())
                .businessAddress(request.getBusinessAddress())
                .yardAddress(request.getYardAddress())
                .build();

        Member savedMember = memberRepository.save(member);
        return memberMapper.toDTO(savedMember);
    }
}
