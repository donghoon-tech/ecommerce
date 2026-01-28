package com.example.ecommerce.controller;

import com.example.ecommerce.dto.UserDTO;
import com.example.ecommerce.security.JwtTokenProvider;
import com.example.ecommerce.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            String username = authentication.getName();
            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_USER");

            String token = jwtTokenProvider.createToken(username, role);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", username);
            response.put("role", role);

            return ResponseEntity.ok(response);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("message", "아이디 또는 비밀번호가 올바르지 않습니다."));
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인에 실패했습니다."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/find-id")
    public ResponseEntity<Map<String, String>> findId(@RequestBody FindIdRequest request) {
        String username = authService.findId(request.getPhone());
        return ResponseEntity.ok(Map.of("username", username));
    }

    @PostMapping("/check-phone")
    public ResponseEntity<Map<String, Boolean>> checkPhone(@RequestBody CheckPhoneRequest request) {
        boolean exists = authService.checkPhoneExists(request.getPhone());
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/check-user-phone")
    public ResponseEntity<Map<String, Boolean>> checkUserAndPhone(@RequestBody CheckUserPhoneRequest request) {
        boolean exists = authService.checkUserAndPhoneExists(request.getUsername(), request.getPhone());
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        String tempPassword = authService.resetPassword(request.getUsername(), request.getPhone());
        return ResponseEntity.ok(Map.of("tempPassword", tempPassword));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String phone; // 필수
        private String companyName;
        private String email;
        private String businessNumber;
        private String businessAddress;
        private String yardAddress;
    }

    @Data
    public static class FindIdRequest {
        private String phone;
    }

    @Data
    public static class CheckPhoneRequest {
        private String phone;
    }

    @Data
    public static class CheckUserPhoneRequest {
        private String username;
        private String phone;
    }

    @Data
    public static class ResetPasswordRequest {
        private String username;
        private String phone;
    }
}