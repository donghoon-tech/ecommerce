package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.MemberDTO;
import com.example.ecommerce.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
    public MemberDTO toDTO(Member member) {
        if (member == null) return null;
        
        return MemberDTO.builder()
                .id(member.getId())
                .username(member.getUsername())
                .role(member.getRole())
                .companyName(member.getCompanyName())
                .phone(member.getPhone())
                .email(member.getEmail())
                .businessNumber(member.getBusinessNumber())
                .businessAddress(member.getBusinessAddress())
                .yardAddress(member.getYardAddress())
                .build();
    }
}