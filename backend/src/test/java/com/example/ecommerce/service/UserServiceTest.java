package com.example.ecommerce.service;

import com.example.ecommerce.dto.UserDTO;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.mapper.UserMapper;
import com.example.ecommerce.repository.BusinessProfileRepository;
import com.example.ecommerce.repository.RoleRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BusinessProfileRepository businessProfileRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("관리자가 사용자의 역할을 변경하면 DB의 RoleRepository를 조회하여 부여한다")
    void updateUserRole_success() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("testuser").role(new Role()).build();
        Role newRole = Role.builder().name("ADMIN").build();
        UserDTO expectedDTO = UserDTO.builder().username("testuser").role("ADMIN").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(roleRepository.findByName("ADMIN")).willReturn(Optional.of(newRole));
        given(userMapper.toDTO(any(User.class), any())).willReturn(expectedDTO);

        // When
        UserDTO result = userService.updateUserRole(userId, "admin");

        // Then
        assertThat(result.getRole()).isEqualTo("ADMIN");
        assertThat(user.getRole().getName()).isEqualTo("ADMIN");
    }
}
