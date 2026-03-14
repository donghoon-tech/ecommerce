package com.example.ecommerce.service;

import com.example.ecommerce.dto.PermissionDTO;
import com.example.ecommerce.dto.RoleDTO;
import com.example.ecommerce.entity.Permission;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.repository.PermissionRepository;
import com.example.ecommerce.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * 전체 Role 목록 조회 (각 Role에 바인딩된 Permission 포함)
     */
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 특정 Role 조회
     */
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("해당 Role을 찾을 수 없습니다."));
        return toDTO(role);
    }

    /**
     * 전체 Permission 목록 조회 (읽기 전용)
     */
    @Transactional(readOnly = true)
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> PermissionDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Role 생성
     */
    public RoleDTO createRole(String name, String description, List<String> permissionNames) {
        if (roleRepository.findByName(name).isPresent()) {
            throw new RuntimeException("이미 존재하는 Role 이름입니다: " + name);
        }

        Set<Permission> permissions = resolvePermissions(permissionNames);

        Role role = Role.builder()
                .name(name.toUpperCase())
                .description(description)
                .permissions(permissions)
                .build();

        return toDTO(roleRepository.save(role));
    }

    /**
     * Role의 Permission 바인딩 수정 (이름, 설명 포함)
     */
    public RoleDTO updateRole(UUID roleId, String name, String description, List<String> permissionNames) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("해당 Role을 찾을 수 없습니다."));

        if (name != null && !name.isBlank()) {
            role.setName(name.toUpperCase());
        }
        if (description != null) {
            role.setDescription(description);
        }
        if (permissionNames != null) {
            role.setPermissions(resolvePermissions(permissionNames));
        }

        return toDTO(role);
    }

    /**
     * Role 삭제 (기본 Role인 UNVERIFIED, USER, ADMIN은 삭제 불가)
     */
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("해당 Role을 찾을 수 없습니다."));

        List<String> protectedRoles = List.of("UNVERIFIED", "USER", "ADMIN");
        if (protectedRoles.contains(role.getName())) {
            throw new RuntimeException("기본 Role(" + role.getName() + ")은 삭제할 수 없습니다.");
        }

        roleRepository.delete(role);
    }

    private Set<Permission> resolvePermissions(List<String> permissionNames) {
        if (permissionNames == null || permissionNames.isEmpty()) {
            return new HashSet<>();
        }
        Set<Permission> found = permissionRepository.findByNameIn(permissionNames);
        if (found.size() != permissionNames.size()) {
            throw new RuntimeException("일부 Permission 이름이 올바르지 않습니다.");
        }
        return found;
    }

    private RoleDTO toDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().stream()
                        .map(Permission::getName)
                        .sorted()
                        .collect(Collectors.toList()))
                .build();
    }
}
