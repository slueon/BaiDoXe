package com.baidoxe.parking_iot.controller;
import com.baidoxe.parking_iot.entity.User;
import com.baidoxe.parking_iot.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") 
public class AuthController {
    @Autowired
    private AuthRepository authRepository;
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        String usernameIn = loginRequest.get("username");
        String passwordIn = loginRequest.get("password");
        try {
            User user = authRepository.findByUsername(usernameIn);
            if (user != null && user.getPasswordHash().equals(passwordIn)) {
                response.put("success", true);
                response.put("message", "Đăng nhập thành công" + user.getFullName());
                response.put("role", user.getRole());
                return ResponseEntity.status(200).body(response);
            } else {
                response.put("success", false);
                response.put("message", "Tài khoản hoặc mật khẩu không đúng");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã đăng xuất thành công"); 
        return ResponseEntity.status(200).body(response);
    }
}