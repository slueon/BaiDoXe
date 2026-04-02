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
            // ================= XE VÀO BÃI =================
            ParkingHistory newSession = new ParkingHistory();
            newSession.setRfidCard(cardOpt.get());
            newSession.setEntryTime(LocalDateTime.now());
            newSession.setStatus("IN");
            historyRepo.save(newSession);
            
            response.put("success", true);
            response.put("action", "ENTRY");
            // Đã xóa biển số, chỉ in mã thẻ
            response.put("message", "Mở barie! Thẻ " + cardId + " vào bãi.");
        } else {
            // ================= XE RA KHỎI BÃI (TÍNH TIỀN) =================
            LocalDateTime now = LocalDateTime.now();
            activeSession.setExitTime(now);
            activeSession.setStatus("OUT");
            
            // LOGIC TÍNH TIỀN:
            // Lấy ngày vào và ngày ra để so sánh
            java.time.LocalDate entryDate = activeSession.getEntryTime().toLocalDate();
            java.time.LocalDate exitDate = now.toLocalDate();
            
            // Tính số ngày chênh lệch
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(entryDate, exitDate);
            
            double fee;
            if (daysBetween == 0) {
                fee = 5000.0; // Lấy trong ngày: 5k
            } else {
                fee = 10000.0; // Sang ngày hôm sau: 10k (Hoặc sếp có thể nhân lên: 5000 + 5000 * daysBetween)
            }
            
            activeSession.setFee(fee); 
            historyRepo.save(activeSession);
            
            response.put("success", true);
            response.put("action", "EXIT");
            response.put("message", "Mở barie! Thu tiền: " + fee + " VNĐ. Tạm biệt quý khách!");
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