package com.baidoxe.parking_iot.controller;

import com.baidoxe.parking_iot.entity.ParkingSpot;
import com.baidoxe.parking_iot.repository.ParkingSpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Khai báo đây là bồi bàn chuyên trả về dữ liệu chuẩn JSON cho Web
@RequestMapping("/api/spots") // Địa chỉ để Web nó gọi vào lấy hàng
@CrossOrigin(origins = "*") // Bùa chú cực quan trọng: Cho phép Web ở máy khác gọi vào mà không bị chặn
public class ParkingSpotController {

    @Autowired
    private ParkingSpotRepository parkingSpotRepository; // Gọi thợ xây ra để moi dữ liệu từ DB

    // Khi Web nó gọi vào đường dẫn /api/spots, hàm này sẽ moi toàn bộ bãi đỗ xe ném ra
    @GetMapping
    public List<ParkingSpot> getAllSpots() {
        return parkingSpotRepository.findAll();
    }
}