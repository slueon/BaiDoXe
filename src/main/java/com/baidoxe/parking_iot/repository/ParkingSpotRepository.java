package com.baidoxe.parking_iot.repository;

import com.baidoxe.parking_iot.entity.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Integer> {
    // Hàm này cực quan trọng: Để tí nữa cảm biến MQTT gửi mã về, mình biết đường lôi đúng ô đỗ ra update!
    ParkingSpot findBySensorId(String sensorId);
}