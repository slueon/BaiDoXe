package com.baidoxe.parking_iot.controller;

import com.baidoxe.parking_iot.entity.User;
import com.baidoxe.parking_iot.repository.UserRepository;
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
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        String usernameIn = loginRequest.get("username");
        String passwordIn = loginRequest.get("password"); 
        
        try {
            User user = userRepository.findByUsername(usernameIn);
            
            if (user != null && user.getPasswordHash() != null && passwordIn.equals(user.getPasswordHash())) {
                response.put("success", true);
                response.put("message", "Đăng nhập thành công " + user.getFullName() + "!");
                response.put("role", user.getRole());
                
                // Sinh ra một token giả để Frontend lưu vào localStorage (để checkAuth hoạt động)
                response.put("token", "fake-jwt-token-" + user.getUsername());
                
                return ResponseEntity.ok(response); 
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
        
        // Với hệ thống dùng token lưu ở phía Frontend, backend logout chỉ đơn giản trả về thành công 
        // (Trừ khi bạn lưu token trong database / blacklist để chặn truy cập ngay lập tức)
        
        response.put("success", true);
        response.put("message", "Đăng xuất thành công!");
        return ResponseEntity.ok(response);
    }
}