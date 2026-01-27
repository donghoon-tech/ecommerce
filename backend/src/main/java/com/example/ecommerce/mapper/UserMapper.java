package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.UserDTO;
import com.example.ecommerce.entity.BusinessProfile;
import com.example.ecommerce.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    // password 필드는 DTO에 없으므로 매핑 설정 불필요
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "representativePhone", source = "user.representativePhone")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "role", source = "user.role")
    @Mapping(target = "businessNumber", source = "user.businessNumber")
    @Mapping(target = "isActive", source = "user.active") // Lombok @Builder.Default boolean isActive -> isActive() ->
                                                          // 'active' property
    @Mapping(target = "createdAt", source = "user.createdAt")
    // Business Profile Fields
    @Mapping(target = "companyName", source = "profile.businessName")
    @Mapping(target = "officeAddress", source = "profile.officeAddress")
    @Mapping(target = "businessStatus", source = "profile.status")
    UserDTO toDTO(User user, BusinessProfile profile);

    // Profile 없이 User만 있을 경우
    @Mapping(target = "companyName", ignore = true)
    @Mapping(target = "officeAddress", ignore = true)
    @Mapping(target = "businessStatus", ignore = true)
    @Mapping(target = "isActive", source = "user.active")
    UserDTO toDTO(User user);
}
