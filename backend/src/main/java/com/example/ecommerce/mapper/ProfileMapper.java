package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.ProfileDTO;
import com.example.ecommerce.entity.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileDTO toDTO(Profile profile);

    Profile toEntity(ProfileDTO dto);
}
