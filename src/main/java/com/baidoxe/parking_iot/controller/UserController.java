package com.baidoxe.parking_iot.controller;

import com.baidoxe.parking_iot.entity.User;
import com.baidoxe.parking_iot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User loginRequest) {
        Map<String, Object> response = new HashMap<>();
        User user = userRepository.findByUsername(loginRequest.getUsername());
        
        if (user != null && user.getPasswordHash().equals(loginRequest.getPasswordHash())) {
            response.put("success", true);
            response.put("message", "Đăng nhập thành công! Chào sếp " + user.getFullName());
            response.put("role", user.getRole());
        } else {
            response.put("success", false);
            response.put("message", "Sai tài khoản hoặc mật khẩu rồi đại vương ơi!");
        }
        return response;
    }
}