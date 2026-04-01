package com.baidoxe.parking_iot.controller;

import com.baidoxe.parking_iot.entity.ParkingHistory;
import com.baidoxe.parking_iot.entity.RfidCard;
import com.baidoxe.parking_iot.repository.ParkingHistoryRepository;
import com.baidoxe.parking_iot.repository.RfidCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity; // Bơm thêm cái này để trả về HTTP status
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rfid") // ĐƯỜNG DẪN GỐC LÀ /api/rfid NHÉ
@CrossOrigin(origins = "*")
public class RfidController {

    @Autowired private RfidCardRepository cardRepo;
    @Autowired private ParkingHistoryRepository historyRepo;

    // =========================================================
    // 1. API CŨ CỦA SẾP (Quẹt thẻ xe ra/vào) - GIỮ NGUYÊN KHÔNG ĐỤNG CHẠM
    // =========================================================
    @PostMapping("/scan/{cardId}")
    public Map<String, Object> scanCard(@PathVariable String cardId) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<RfidCard> cardOpt = cardRepo.findById(cardId);
        if (cardOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Thẻ giả hoặc chưa đăng ký!");
            return response;
        }

        ParkingHistory activeSession = historyRepo.findByRfidCard_CardIdAndStatus(cardId, "IN");

        if (activeSession == null) {
            // Xe VÀO bãi
            ParkingHistory newSession = new ParkingHistory();
            newSession.setRfidCard(cardOpt.get());
            newSession.setEntryTime(LocalDateTime.now());
            newSession.setStatus("IN");
            historyRepo.save(newSession);
            
            response.put("success", true);
            response.put("action", "ENTRY");
            response.put("message", "Mở barie! Biển số: " + cardOpt.get().getLicensePlate() + " vào bãi.");
        } else {
            // Xe RA khỏi bãi
            activeSession.setExitTime(LocalDateTime.now());
            activeSession.setStatus("OUT");
            activeSession.setFee(5000.0); // Thu đồng giá 5k cho dễ test
            historyRepo.save(activeSession);
            
            response.put("success", true);
            response.put("action", "EXIT");
            response.put("message", "Mở barie! Thu tiền: 5000 VND. Tạm biệt quý khách!");
        }
        return response;
    }

    // =========================================================
    // 2. API MỚI BƠM THÊM ĐỂ QUẢN LÝ THẺ TRÊN WEB
    // =========================================================

    // API Lấy toàn bộ danh sách thẻ đổ ra bảng
    @GetMapping
    public ResponseEntity<?> getAllCards() {
        return ResponseEntity.ok(cardRepo.findAll());
    }

    // API Thêm thẻ mới
    @PostMapping
    public ResponseEntity<Map<String, Object>> addCard(@RequestBody RfidCard newCard) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Check xem mã thẻ (cardId) này có ai xài chưa
            Optional<RfidCard> existCard = cardRepo.findById(newCard.getCardId());
            if (existCard.isPresent()) {
                response.put("success", false);
                response.put("message", "Mã thẻ này đã tồn tại rồi sếp ơi!");
                return ResponseEntity.status(400).body(response);
            }

            cardRepo.save(newCard);
            response.put("success", true);
            response.put("message", "Đã đăng ký thẻ RFID thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi rớt mạng: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}