package com.baidoxe.parking_iot.controller;

import com.baidoxe.parking_iot.entity.User;
import com.baidoxe.parking_iot.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Mở cửa cho Frontend gọi sang thoải mái
public class UserController {

    private UserRepository userRepository = new UserRepository();

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
                response.put("message", "Sai tài khoản hoặc mật khẩu rồi!");
                
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

    // ================= 1. API LẤY DANH SÁCH NHÂN VIÊN =================
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        // Gọi thẳng lệnh findAll() của JPA để lôi hết data trong bảng users ra
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ================= 2. API THÊM NHÂN VIÊN MỚI =================
    @PostMapping
    public ResponseEntity<Map<String, Object>> addUser(@RequestBody User newUser) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Check xem thằng này đã tồn tại trong DB chưa (chống trùng username)
            User existUser = userRepository.findByUsername(newUser.getUsername());
            if (existUser != null) {
                response.put("success", false);
                response.put("message", "Tên đăng nhập này có người xài rồi sếp ơi!");
                return ResponseEntity.status(400).body(response);
            }
            
            // Chưa có thì lưu vào DB (Pass hiện tại anh em mình vẫn đang chơi hệ pass trần nhé)
            userRepository.save(newUser);
            
            response.put("success", true);
            response.put("message", "Đã thêm nhân viên thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi rớt mạng rớt server: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ================= 3. API SỬA THÔNG TIN NHÂN VIÊN =================
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Integer id, @RequestBody User updatedUser) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Tìm xem ông nhân viên này có trong DB không
            User existingUser = userRepository.findById(id).orElse(null);
            if (existingUser == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy nhân viên này sếp ơi!");
                return ResponseEntity.status(404).body(response);
            }
            
            // Cập nhật thông tin mới (Giả sử chỉ cho sửa Tên và Quyền, không sửa Username)
            existingUser.setFullName(updatedUser.getFullName());
            existingUser.setRole(updatedUser.getRole());
            
            // Nếu có nhập pass mới thì mới đổi, không thì giữ nguyên
            if (updatedUser.getPasswordHash() != null && !updatedUser.getPasswordHash().isEmpty()) {
                existingUser.setPasswordHash(updatedUser.getPasswordHash());
            }

            userRepository.save(existingUser);
            
            response.put("success", true);
            response.put("message", "Đã cập nhật thông tin thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ================= 4. API XÓA/KHÓA NHÂN VIÊN =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Chỗ này làm đơn giản là Xóa thẳng khỏi DB luôn nhé
            userRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Đã tiễn nhân viên ra chuồng gà!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi rồi, chắc nhân viên này đang dính líu đến lịch sử bãi xe nên DB không cho xóa!");
            return ResponseEntity.status(500).body(response);
        }
    }
}