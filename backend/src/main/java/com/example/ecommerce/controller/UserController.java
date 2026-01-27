package com.example.ecommerce.controller;

import com.example.ecommerce.dto.UserDTO;
import com.example.ecommerce.dto.UserUpdateRequest;
import com.example.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyInfo(Principal principal) {
        return ResponseEntity.ok(userService.getMyInfo(principal.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMyInfo(Principal principal, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateMyInfo(principal.getName(), request));
    }
}
