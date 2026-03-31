package com.baidoxe.parking_iot.controller;

import com.baidoxe.parking_iot.entity.User;
import com.baidoxe.parking_iot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Mở cửa cho Frontend gọi sang thoải mái
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        // 1. Rút username và password từ cái form Frontend (nãy anh em mình code biến "password")
        String usernameIn = loginRequest.get("username");
        String passwordIn = loginRequest.get("password"); 
        
        try {
            // 2. Chọc vào DB tìm User
            User user = userRepository.findByUsername(usernameIn);
            
            // 3. Kiểm tra xem User có tồn tại không, và Pass gõ vào có khớp với Pass trong DB không
            if (user != null && user.getPasswordHash() != null && user.getPasswordHash().equals(passwordIn)) {
                // TRÚNG MÁNH -> Cấp phép cho vào!
                response.put("success", true);
                response.put("message", "Đăng nhập thành công! Chào sếp " + user.getFullName());
                response.put("role", user.getRole());
                
                // Bắt buộc phải có cái Token fake này để Frontend nhét vào localStorage
                response.put("token", "chia_khoa_vip_ne_hihi"); 
                
                // Trả về mã 200 OK
                return ResponseEntity.ok(response); 
            } else {
                // SAI PASS HOẶC KHÔNG TỒN TẠI -> Đuổi về!
                response.put("success", false);
                response.put("message", "Sai tài khoản hoặc mật khẩu rồi đại vương ơi!");
                
                // Trả về mã 401 Unauthorized (Frontend thấy mã này sẽ hiện chữ đỏ)
                return ResponseEntity.status(401).body(response); 
            }
        } catch (Exception e) {
            // Lỡ có sập server hay lỗi DB thì báo luôn
            response.put("success", false);
            response.put("message", "Lỗi Backend cmnr sếp ơi: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}