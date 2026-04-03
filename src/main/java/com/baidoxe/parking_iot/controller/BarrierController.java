package com.baidoxe.parking_iot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/barrier")
@CrossOrigin(origins = "*")
public class BarrierController {

    // API Mở barie thủ công
    @PostMapping("/open")
    public ResponseEntity<Map<String, Object>> openBarrier() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Ở đây sau này có thể chèn code gửi tín hiệu MQTT/HTTP xuống mạch ESP32
            
            response.put("success", true);
            response.put("message", "🚧 Lệnh đã truyền đi: Barie đang được mở!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi rớt mạng, không kết nối được với Barie!");
            return ResponseEntity.status(500).body(response);
        }
    }
}