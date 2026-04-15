package com.baidoxe.parking_iot.controller;

import com.baidoxe.parking_iot.entity.ParkingHistory;
import com.baidoxe.parking_iot.entity.ParkingSpot;
import com.baidoxe.parking_iot.entity.RfidCard;
import com.baidoxe.parking_iot.repository.ParkingHistoryRepository;
import com.baidoxe.parking_iot.repository.ParkingSpotRepository;
import com.baidoxe.parking_iot.repository.RfidCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rfid")
@CrossOrigin(origins = "*")
public class RfidController {

    @Autowired
    private RfidCardRepository cardRepo;

    @Autowired
    private ParkingHistoryRepository historyRepo;

    // GỌI THÊM ÔNG NÀY ĐỂ ĐI TÌM Ô TRỐNG
    @Autowired
    private ParkingSpotRepository spotRepo; 

    @PostMapping("/scan/{cardId}")
    public Map<String, Object> scanCard(@PathVariable String cardId) {
        Map<String, Object> response = new HashMap<>();
        
        // 1. KIỂM TRA THẺ CÓ HỢP LỆ KHÔNG
        Optional<RfidCard> cardOpt = cardRepo.findById(cardId);
        if (cardOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Thẻ giả hoặc chưa đăng ký!");
            return response;
        }

        ParkingHistory activeSession = historyRepo.findByRfidCard_CardIdAndStatus(cardId, "IN");

        if (activeSession == null) {
            // ================= XE VÀO BÃI =================
            
            // 2. TÌM Ô TRỐNG
            List<ParkingSpot> allSpots = spotRepo.findAll();
            ParkingSpot availableSpot = null;
            for (ParkingSpot spot : allSpots) {
                if (!spot.getIsOccupied()) { // Nếu thấy ô nào isOccupied = false
                    availableSpot = spot;
                    break; // Lấy luôn ô đầu tiên tìm thấy
                }
            }

            // Xử lý logic nếu bãi đỗ xe đã đầy
            if (availableSpot == null) {
                response.put("success", false);
                response.put("message", "Bãi xe đã ĐẦY! Không thể mở Barie.");
                return response;
            }

            // 3. GHI GIỜ VÀO (ENTRY TIME)
            ParkingHistory newSession = new ParkingHistory();
            newSession.setRfidCard(cardOpt.get());
            newSession.setEntryTime(LocalDateTime.now());
            newSession.setStatus("IN");
            
            // Nếu Database của sếp có liên kết khóa ngoại spot_id trong bảng lịch sử, 
            // sếp có thể mở comment dòng dưới này:
            // newSession.setSpotId(availableSpot.getSpotId()); 
            
            historyRepo.save(newSession);
            
            // 4. BÁO THIẾT BỊ MỞ BARRIER
            response.put("success", true);
            response.put("action", "ENTRY");
            response.put("message", "Mở barie! Mời đỗ xe vào: " + availableSpot.getSpotName());

        } else {
            // ================= XE RA KHỎI BÃI =================
            
            // 1. TÌM LỊCH SỬ GIỜ VÀO
            LocalDateTime now = LocalDateTime.now();
            activeSession.setExitTime(now); // Cập nhật giờ ra
            
            // 2. TRỪ ĐI GIỜ RA ĐỂ TÍNH PHÍ
            java.time.LocalDate entryDate = activeSession.getEntryTime().toLocalDate();
            java.time.LocalDate exitDate = now.toLocalDate();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(entryDate, exitDate);
            
            double fee = (daysBetween == 0) ? 5000.0 : 10000.0;
            activeSession.setFee(fee); 
            
            // 3. CẬP NHẬT TRẠNG THÁI "ĐÃ RA"
            activeSession.setStatus("OUT");
            historyRepo.save(activeSession);
            
            // 4. BÁO MỞ BARRIER
            response.put("success", true);
            response.put("action", "EXIT");
            response.put("message", "Mở barie! Thu phí: " + fee + " đ. Tạm biệt!");
        }
        return response;
    }

    // 1. API Lấy danh sách toàn bộ thẻ để hiển thị lên Web
    @GetMapping
    public List<RfidCard> getAllCards() {
        return cardRepo.findAll();
    }

    // 2. API Thêm thẻ mới (để cái nút "Gán Thẻ Mới" màu xanh nó hoạt động)
    @PostMapping
    public Map<String, Object> addCard(@RequestBody RfidCard newCard) {
        Map<String, Object> response = new HashMap<>();
        try {
            cardRepo.save(newCard);
            response.put("success", true);
            response.put("message", "Thêm thẻ thành công!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: Thẻ này có thể đã tồn tại!");
        }
        return response;
    }

    // API Xóa thẻ RFID
    @DeleteMapping("/{cardId}")
    public Map<String, Object> deleteCard(@PathVariable String cardId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Thử xóa thẻ trong Database
            cardRepo.deleteById(cardId);
            
            response.put("success", true);
            response.put("message", "Đã xóa thẻ thành công khỏi hệ thống!");
        } catch (Exception e) {
            // Nếu xóa thất bại (do dính khóa ngoại MySQL vì thẻ đã từng quẹt xe)
            response.put("success", false);
            response.put("message", "Không thể xóa! Thẻ này đã có lịch sử ra/vào bãi. Chỉ có thể Khóa nó lại.");
        }
        return response;
    }

    // API Khóa / Mở khóa thẻ (Tận dụng biến isActive có sẵn)
    @PutMapping("/toggle/{cardId}")
    public Map<String, Object> toggleCard(@PathVariable String cardId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<RfidCard> cardOpt = cardRepo.findById(cardId);
            if (cardOpt.isPresent()) {
                RfidCard card = cardOpt.get();
                
                // Lấy trạng thái hiện tại (nếu null thì mặc định là true)
                boolean currentStatus = card.getIsActive() != null ? card.getIsActive() : true;
                
                // Đảo ngược trạng thái
                card.setIsActive(!currentStatus);
                cardRepo.save(card);
                
                response.put("success", true);
                response.put("message", !currentStatus ? "Đã MỞ KHÓA thẻ!" : "Đã KHÓA thẻ!");
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy thẻ!");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi Server!");
        }
        return response;
    }
}