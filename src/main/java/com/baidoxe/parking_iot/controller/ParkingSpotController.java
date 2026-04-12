package com.baidoxe.parking_iot.controller;

import com.baidoxe.parking_iot.entity.ParkingSpot;
import com.baidoxe.parking_iot.repository.ParkingSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Đã bơm thêm
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; // Đã bơm thêm
import java.util.List;
import java.util.Map; // Đã bơm thêm

@RestController 
@RequestMapping("/api/spots") 
@CrossOrigin(origins = "*") 
public class ParkingSpotController {

    @Autowired
    private ParkingSpotRepository parkingSpotRepository; 

    // ===============================================
    // 1. API CŨ: MOI TOÀN BỘ BÃI ĐỖ XE
    // ===============================================
    @GetMapping
    public List<ParkingSpot> getAllSpots() {
        return parkingSpotRepository.findAll();
    }

    // ===============================================
    // 2. API MỚI: THÊM Ô ĐỖ XE
    // ===============================================
    @PostMapping
    public ResponseEntity<Map<String, Object>> addSpot(@RequestBody ParkingSpot newSpot) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Mặc định ô mới xây ra là chưa có xe (TRỐNG)
            newSpot.setIsOccupied(false); 
            parkingSpotRepository.save(newSpot);
            
            response.put("success", true);
            response.put("message", "Đã quy hoạch thêm ô đỗ mới thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ===============================================
    // 3. API MỚI: XÓA Ô ĐỖ XE (GIẢI TỎA)
    // ===============================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSpot(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Gọi thợ xây ra đập bỏ ô này trong DB
            parkingSpotRepository.deleteById(id);
            
            response.put("success", true);
            response.put("message", "Da bo o do nay thanh cong!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Loi! Dang co xe do o day hoac dinh du lieu lich su!");
            return ResponseEntity.status(500).body(response);
        }
    }
}